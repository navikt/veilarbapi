package no.nav.poao.rest

import com.auth0.jwt.JWT
import com.google.gson.Gson
import com.nimbusds.jwt.SignedJWT
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.common.types.identer.NavIdent
import no.nav.poao.util.setupEnvironment
import no.nav.poao.veilarbapi.module
import no.nav.poao.veilarbapi.oppfolging.*
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.withMockOAuth2Server
import no.nav.veilarbapi.model.Oppfolgingsinfo
import org.assertj.core.api.Assertions
import org.junit.Test
import kotlin.test.assertEquals

class ArbeidsoppfolgingRoutesITest2 {

    @Test
    fun `hent oppfolgingsinfo`() {
        val underOppfolgingDTO = UnderOppfolgingDTO(true)
        val underOppfolgingMock = Gson().toJson(underOppfolgingDTO)

        val veilederDTO = VeilederDTO(NavIdent("z123456"))
        val veilederMock = Gson().toJson(veilederDTO)

        val oppfolgingsenhetDTO = OppfolgingsenhetDTO("NAV Grünerløkka", "1234")
        val oppfolgingsenhetMock = Gson().toJson(oppfolgingsenhetDTO)

        val httpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    when (request.url.encodedPath) {
                        "/veilarboppfolging/api/v2/oppfolging" -> {
                            checkDownstreamTokenContent(request)
                            respondOk(underOppfolgingMock)
                        }
                        "/veilarboppfolging/api/v2/veileder" -> respondOk(veilederMock)
                        "/veilarboppfolging/api/person/oppfolgingsenhet" -> respondOk(oppfolgingsenhetMock)
                        else -> error("Unhandled ${request.url.encodedPath}")
                    }
                }
            }
        }

        withMockOAuth2Server {
            val initialToken: SignedJWT = this.issueToken(subject = "enduser", audience = "client_id")

            withTestApplication({
                setupEnvironment(this@withMockOAuth2Server)
                module(Configuration(veilarboppfolgingConfig = Configuration.VeilarboppfolgingConfig(httpClient = httpClient)))
            }) {
                with(handleRequest(HttpMethod.Get, "/v1/oppfolging/info?aktorId=123") {
                    this.addHeader("Authorization", "Bearer ${initialToken.serialize()}")
                }) {
                    assertEquals(HttpStatusCode.OK, response.status())
                    val oppfolgingsinfo =
                        no.nav.veilarbapi.JSON.deserialize<Oppfolgingsinfo>(
                            response.content,
                            Oppfolgingsinfo::class.java
                        )
                    assertEquals(underOppfolgingDTO.erUnderOppfolging, oppfolgingsinfo.underOppfolging)
                }
            }
        }

    }

    private fun checkDownstreamTokenContent(request: HttpRequestData) {
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