package no.nav.poao.rest

import com.auth0.jwt.JWT
import com.google.gson.Gson
import com.nimbusds.jwt.SignedJWT
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.common.types.identer.NavIdent
import no.nav.poao.util.*
import no.nav.poao.util.setupEnvironment
import no.nav.poao.util.withWiremockServer
import no.nav.poao.veilarbapi.module
import no.nav.poao.veilarbapi.oppfolging.OppfolgingsenhetDTO
import no.nav.poao.veilarbapi.oppfolging.OppfolgingsperiodeDTO
import no.nav.poao.veilarbapi.oppfolging.UnderOppfolgingDTO
import no.nav.poao.veilarbapi.oppfolging.VeilederDTO
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.poao.veilarbapi.setup.http.DownstreamAuthorization
import no.nav.security.mock.oauth2.withMockOAuth2Server
import no.nav.veilarbapi.model.Oppfolgingsinfo
import org.assertj.core.api.Assertions
import org.junit.Test
import org.threeten.bp.OffsetDateTime
import java.util.*
import kotlin.test.assertEquals

class ArbeidsoppfolgingRoutesITest {

    init {
        no.nav.veilarbaktivitet.JSON()
        no.nav.veilarbdialog.JSON()
        no.nav.veilarbapi.JSON()
    }

    @Test
    fun `hent oppfolgingsinfo med wiremock for eksterne kall`() {
        val underOppfolgingDTO = UnderOppfolgingDTO(true)
        val veilederDTO = VeilederDTO(NavIdent("z999999"))
        val oppfolgingsenhetDTO = OppfolgingsenhetDTO(navn = "Nav Grunerløkka", "1234")

        withWiremockServer {
            stubUnderOppfolging(this, underOppfolgingDTO)
            stubVeileder(this, veilederDTO)
            stubOppfolgingsEnhet(this, oppfolgingsenhetDTO)

            withMockOAuth2Server {
                val token = this.issueToken(subject = "enduser", audience = "client_id")

                testApplication {
                    application {
                        setupEnvironment(this@withMockOAuth2Server)
                        module()
                    }
                    val response = client.get("/v1/oppfolging/info?aktorId=123") {
                        header(HttpHeaders.Authorization, "Bearer ${token.serialize()}")
                    }
                    assertEquals(HttpStatusCode.OK, response.status)

                    val oppfolgingsinfo =
                        no.nav.veilarbapi.JSON.deserialize<Oppfolgingsinfo>(
                            response.bodyAsText(),
                            Oppfolgingsinfo::class.java
                        )
                    Assertions.assertThat(oppfolgingsinfo.underOppfolging).isEqualTo(underOppfolgingDTO.erUnderOppfolging)
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

        val veilarboppfolgingMockClient = createMockClient { request ->
            when (request.url.encodedPath) {
                "/veilarboppfolging/api/v2/oppfolging" -> {
                    checkDownstreamTokenContent(request, "api://local.pto.veilarboppfolging/.default")
                    respondOk(underOppfolgingMock)
                }
                "/veilarboppfolging/api/v2/veileder" -> respondOk(veilederMock)
                "/veilarboppfolging/api/person/oppfolgingsenhet" -> respondOk(oppfolgingsenhetMock)
                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }

        withMockOAuth2Server {
            val initialToken: SignedJWT = this.issueToken(subject = "enduser", audience = "client_id")

            testApplication {
                application {
                    setupEnvironment(this@withMockOAuth2Server)
                    module(Configuration(veilarboppfolgingConfig = Configuration.VeilarboppfolgingConfig(httpClient = veilarboppfolgingMockClient)))
                }
                val response = client.get("/v1/oppfolging/info?aktorId=123") {
                    header("Authorization", "Bearer ${initialToken.serialize()}")
                }
                val oppfolgingsinfo =
                    no.nav.veilarbapi.JSON.deserialize<Oppfolgingsinfo>(
                        response.bodyAsText(),
                        Oppfolgingsinfo::class.java
                    )
                assertEquals(underOppfolgingDTO.erUnderOppfolging, oppfolgingsinfo.underOppfolging)
            }
        }
    }

    @Test
    fun `hent oppfolgingsperiode med mockengine client for eksterne tjenester`() {
        val uuid = UUID.randomUUID()
        val oppfolgingsperiode = listOf(OppfolgingsperiodeDTO(uuid, "aktorid", null, OffsetDateTime.now().minusDays(1), null))
        val internAktiviteter = listOf(InternAktivitetBuilder.nyAktivitet("egenaktivitet").oppfolgingsperiodeId(uuid).aktivitetId("3"))
        val internDialoger = listOf(InternDialogBuilder.nyDialog().oppfolgingsperiodeId(uuid).aktivitetId("3"))

        val mockOppfolgingsperiode = Gson().toJson(oppfolgingsperiode)
        val mockAktiviteter = no.nav.veilarbaktivitet.JSON.getGson().toJson(internAktiviteter)
        val mockDialoger = no.nav.veilarbdialog.JSON.getGson().toJson(internDialoger)

        val veilarbdialogClient = createMockClient { request ->
            checkDownstreamTokenContent(request, "api://local.pto.veilarbdialog/.default")
            respondOk(mockDialoger)
        }

        val veilarbaktivitetClient = createMockClient { request ->
            checkDownstreamTokenContent(request, "api://local.pto.veilarbaktivitet/.default")
            respondOk(mockAktiviteter)
        }

        val veilarboppfolgingClient = createMockClient { request ->
            checkDownstreamTokenContent(request, "api://local.pto.veilarboppfolging/.default")
            respondOk(mockOppfolgingsperiode)
        }

        withMockOAuth2Server {
            val initialToken: SignedJWT = this.issueToken(subject = "enduser", audience = "client_id")

            testApplication {
                application {
                    setupEnvironment(this@withMockOAuth2Server)
                    module(Configuration(
                        veilarbdialogConfig = Configuration.VeilarbdialogConfig(httpClient = veilarbdialogClient),
                        veilarbaktivitetConfig = Configuration.VeilarbaktivitetConfig(httpClient = veilarbaktivitetClient),
                        veilarboppfolgingConfig = Configuration.VeilarboppfolgingConfig(httpClient = veilarboppfolgingClient)
                    ))
                }
                val response = client.get("/v1/oppfolging/periode?aktorId=123") {
                    header(HttpHeaders.Authorization, "Bearer ${initialToken.serialize()}")
                }
                assertEquals(HttpStatusCode.OK, response.status)
            }
        }
    }


    private fun checkDownstreamTokenContent(request: HttpRequestData, expectedAudience: String) {
        val downstreamAuthString = request.headers[HttpHeaders.DownstreamAuthorization]?.substringAfter("Bearer ")
        val downstreamAuthJwt = JWT.decode(downstreamAuthString)
        Assertions.assertThat(downstreamAuthJwt.subject).isEqualTo("enduser")
        Assertions.assertThat(downstreamAuthJwt.audience).containsExactly(expectedAudience)

        val authString = request.headers[HttpHeaders.Authorization]?.substringAfter("Bearer ")
        val authJwt = JWT.decode(authString)
        Assertions.assertThat(authJwt.subject).isEqualTo("client_id")
        Assertions.assertThat(authJwt.audience).containsExactly("api://local.pto.poao-gcp-proxy/.default")
    }
}