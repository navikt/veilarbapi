package no.nav.poao.rest

import com.auth0.jwt.JWT
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.matching.StringValuePattern
import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.mock.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import no.nav.common.types.identer.NavIdent
import no.nav.poao.IntegrasjonsTest
import no.nav.poao.veilarbapi.oppfolging.*
import no.nav.security.mock.oauth2.withMockOAuth2Server
import no.nav.veilarbapi.model.Oppfolgingsinfo
import org.assertj.core.api.Assertions
import org.junit.Test
import java.util.concurrent.TimeUnit

class ArbeidsoppfolgingRoutesITest {

    companion object {
        init {
            IntegrasjonsTest.setup()
        }
    }

    @Test
    fun `hent oppfolgingsinfo`() {
        val underOppfolgingDTO = UnderOppfolgingDTO(true)
        val underOppfolgingMock = Gson().toJson(underOppfolgingDTO)

        val veilederDTO = VeilederDTO(NavIdent("z123456"))
        val veilederMock = Gson().toJson(veilederDTO)

        val oppfolgingsenhetDTO = OppfolgingsenhetDTO("NAV Grünerløkka", "1234")
        val oppfolgingsenhetMock = Gson().toJson(oppfolgingsenhetDTO)

        IntegrasjonsTest.wireMockServer.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/veilarboppfolging/api/v2/oppfolging"))
                .withQueryParam("aktorId", WireMock.equalTo("123"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(underOppfolgingMock)
                )
        )

        IntegrasjonsTest.wireMockServer.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/veilarboppfolging/api/v2/veileder"))
                .withQueryParam("aktorId", WireMock.equalTo("123"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(veilederMock)
                )
        )

        IntegrasjonsTest.wireMockServer.stubFor(
            WireMock.get(WireMock.urlPathEqualTo("/veilarboppfolging/api/person/oppfolgingsenhet"))
                .withQueryParam("aktorId", WireMock.equalTo("123"))
                .willReturn(
                    WireMock.aResponse()
                        .withStatus(200)
                        .withBody(oppfolgingsenhetMock)
                )
        )

        val token = IntegrasjonsTest.mockOauth2Server.issueToken(subject = "enduser", audience = "clientid")

        val client = HttpClient(OkHttp)
        runBlocking {
            val response = client.get<HttpResponse>("http://0.0.0.0:8080/v1/oppfolging/info?aktorId=123") {
                header("Authorization", "Bearer ${token.serialize()}")
            }
            Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            val oppfolgingsinfo = response.receive<Oppfolgingsinfo>()
        }
    }

    fun checkTokenContent(request: HttpRequestData) {
        val downstreamAuthString = request.headers.get("Downstream-Authorization")?.substringAfter("Bearer ")
        val downstreamAuthJwt = JWT.decode(downstreamAuthString)
        Assertions.assertThat(downstreamAuthJwt.subject).isEqualTo("enduser")
        Assertions.assertThat(downstreamAuthJwt.audience).containsExactly("api://local.pto.veilarboppfolging/.default")

        val authString = request.headers.get("Authorization")?.substringAfter("Bearer ")
        val authJwt = JWT.decode(authString)
        Assertions.assertThat(authJwt.subject).isEqualTo("client_id")
        Assertions.assertThat(authJwt.audience).containsExactly("api://local.pto.poao-gcp-proxy/.default")
    }
}