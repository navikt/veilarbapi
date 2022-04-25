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
import no.nav.poao.util.*
import no.nav.poao.util.setupEnvironment
import no.nav.poao.util.withWiremockServer
import no.nav.poao.veilarbapi.module
import no.nav.poao.veilarbapi.oppfolging.OppfolgingsenhetDTO
import no.nav.poao.veilarbapi.oppfolging.UnderOppfolgingDTO
import no.nav.poao.veilarbapi.oppfolging.VeilederDTO
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.security.mock.oauth2.withMockOAuth2Server
import no.nav.veilarbapi.model.Oppfolgingsinfo
import org.assertj.core.api.Assertions
import org.junit.Test
import kotlin.test.assertEquals

class ArbeidsoppfolgingRoutesITest {


    @Test
    fun `hent oppfolgingsinfo med wiremock for eksterne kall`() {

        val underOppfolgingDto = UnderOppfolgingDTO(true)
        val veilederDTO = VeilederDTO(NavIdent("z999999"))
        val oppfolgingsenhetDTO = OppfolgingsenhetDTO(navn = "Nav Grunerløkka", "1234")

        withWiremockServer {
            withMockOAuth2Server {
                val token = this.issueToken(subject = "enduser", audience = "client_id")
                withTestApplication({
                    setupEnvironment(this@withMockOAuth2Server, this@withWiremockServer)
                    module()
                    stubUnderOppfolging(underOppfolgingDto)
                    stubVeileder(veilederDTO)
                    stubOppfolgingsEnhet(oppfolgingsenhetDTO)
                }) {
                    with(handleRequest(HttpMethod.Get, "/v1/oppfolging/info?aktorId=123") {
                        addHeader(HttpHeaders.Authorization, "Bearer ${token.serialize()}")

                    }) {
                        assertEquals(HttpStatusCode.OK, response.status())
                        val oppfolgingsinfo =
                            no.nav.veilarbapi.JSON.deserialize<Oppfolgingsinfo>(
                                response.content,
                                Oppfolgingsinfo::class.java
                            )
                        Assertions.assertThat(oppfolgingsinfo.underOppfolging).isEqualTo(underOppfolgingDto.erUnderOppfolging)
                    }
                }

            }

        }
    }

    /**
     * Ved å bruke mockengine client og injecte denne i config,
     * kan man plugge inn i klientene og for eksempel validere tokengenerering (tokenprovider kode)
     */
    @Test
    fun `hent oppfolgingsinfo med mockengine client for eksterne tjenester`() {
        val underOppfolgingDTO = UnderOppfolgingDTO(true)
        val underOppfolgingMock = Gson().toJson(underOppfolgingDTO)

        val veilederDTO = VeilederDTO(NavIdent("z123456"))
        val veilederMock = Gson().toJson(veilederDTO)

        val oppfolgingsenhetDTO = OppfolgingsenhetDTO("NAV Grünerløkka", "1234")
        val oppfolgingsenhetMock = Gson().toJson(oppfolgingsenhetDTO)

        val veilarboppfolgingMockClient = HttpClient(MockEngine) {
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
                module(Configuration(veilarboppfolgingConfig = Configuration.VeilarboppfolgingConfig(httpClient = veilarboppfolgingMockClient)))
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