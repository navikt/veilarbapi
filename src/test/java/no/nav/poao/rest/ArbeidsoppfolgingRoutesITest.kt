package no.nav.poao.rest

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import no.nav.common.types.identer.NavIdent
import no.nav.poao.IntegrasjonsTest
import no.nav.poao.veilarbapi.main
import no.nav.poao.veilarbapi.module
import no.nav.poao.veilarbapi.oppfolging.OppfolgingsenhetDTO
import no.nav.poao.veilarbapi.oppfolging.UnderOppfolgingDTO
import no.nav.poao.veilarbapi.oppfolging.VeilederDTO
import no.nav.veilarbapi.model.Oppfolgingsinfo
import org.assertj.core.api.Assertions
import org.junit.Test

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
            get(urlPathEqualTo("/veilarboppfolging/api/v2/oppfolging"))
                .withQueryParam("aktorId", equalTo("123"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(underOppfolgingMock)
                )
        )

        IntegrasjonsTest.wireMockServer.stubFor(
            get(urlPathEqualTo("/veilarboppfolging/api/v2/veileder"))
                .withQueryParam("aktorId", equalTo("123"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(veilederMock)
                )
        )

        IntegrasjonsTest.wireMockServer.stubFor(
            get(urlPathEqualTo("/veilarboppfolging/api/person/oppfolgingsenhet"))
                .withQueryParam("aktorId", equalTo("123"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(oppfolgingsenhetMock)
                )
        )

        val token = IntegrasjonsTest.mockOauth2Server.issueToken(subject = "enduser", audience = "clientid")

        withTestApplication(Application::module) {
            with(handleRequest(HttpMethod.Get, "v1/oppfolging/info?aktorId=123") {
                addHeader(HttpHeaders.Authorization, token.serialize())

            }) {
                val oppfolgingsinfo =
                    no.nav.veilarbapi.JSON.deserialize<Oppfolgingsinfo>(
                        response.content,
                        Oppfolgingsinfo::class.java
                    )
                Assertions.assertThat(oppfolgingsinfo.underOppfolging).isEqualTo(underOppfolgingDTO.erUnderOppfolging)
            }

        }

        val client = HttpClient(OkHttp)
        runBlocking {
            val response = client.get<HttpResponse>("http://0.0.0.0:8080/v1/oppfolging/info?aktorId=123") {
                header("Authorization", "Bearer ${token.serialize()}")
            }
            Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            val oppfolgingsinfo = response.receive<Oppfolgingsinfo>()
            Assertions.assertThat(oppfolgingsinfo.underOppfolging).isEqualTo(underOppfolgingDTO.erUnderOppfolging)
        }
    }

}