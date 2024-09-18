package no.nav.poao.rest

import com.auth0.jwt.JWT
import com.google.gson.Gson
import com.nimbusds.jwt.SignedJWT
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.poao.util.*
import no.nav.poao.util.setupEnvironment
import no.nav.poao.util.withWiremockServer
import no.nav.poao.veilarbapi.module
import no.nav.poao.veilarbapi.oppfolging.*
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
        val veilederDTO = VeilederDTO("z999999")
        val oppfolgingsenhetDTO = OppfolgingsenhetDTO(navn = "Nav Grunerløkka", "1234")

        withWiremockServer {
            stubUnderOppfolging(this, underOppfolgingDTO)
            stubVeileder(this, veilederDTO)
            stubOppfolgingsEnhet(this, oppfolgingsenhetDTO)

            withMockOAuth2Server {
                val token = this.issueToken(subject = "enduser", audience = "client_id")

                testApplication {
                    application {
                        setupEnvironment(this@withMockOAuth2Server, this@withWiremockServer)
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

        val veilederDTO = VeilederDTO("z123456")
        val veilederMock = Gson().toJson(veilederDTO)

        val oppfolgingsenhetDTO = OppfolgingsenhetDTO("NAV Grünerløkka", "1234")
        val oppfolgingsenhetMock = Gson().toJson(oppfolgingsenhetDTO)

        val veilarboppfolgingMockClient = createMockClient { request ->
            when (request.url.encodedPath) {
                "/veilarboppfolging/api/v2/oppfolging" -> {
                    checkBearerTokenContent(request, "api://local.poao.veilarboppfolging/.default")
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

        val mockOppfolgingsperiode = gson().toJson(oppfolgingsperiode)
        val mockAktiviteter = no.nav.veilarbaktivitet.JSON.getGson().toJson(internAktiviteter)
        val mockDialoger = no.nav.veilarbdialog.JSON.getGson().toJson(internDialoger)

        val veilarbdialogClient = createMockClient { request ->
            checkBearerTokenContent(request, "api://local.dab.veilarbdialog/.default")
            respondOk(mockDialoger)
        }

        val veilarbaktivitetClient = createMockClient { request ->
            checkBearerTokenContent(request, "api://local.dab.veilarbaktivitet/.default")
            respondOk(mockAktiviteter)
        }

        val veilarboppfolgingClient = createMockClient { request ->
            checkBearerTokenContent(request, "api://local.poao.veilarboppfolging/.default")
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

    @Test
    fun `kall uten auth token skal feile med 401`() {
        val underOppfolgingDTO = UnderOppfolgingDTO(true)
        val underOppfolgingMock = Gson().toJson(underOppfolgingDTO)

        val veilederDTO = VeilederDTO("z123456")
        val veilederMock = Gson().toJson(veilederDTO)

        val oppfolgingsenhetDTO = OppfolgingsenhetDTO("NAV Grünerløkka", "1234")
        val oppfolgingsenhetMock = Gson().toJson(oppfolgingsenhetDTO)

        val veilarboppfolgingMockClient = createMockClient { request ->
            when (request.url.encodedPath) {
                "/veilarboppfolging/api/v2/oppfolging" -> {
                    checkBearerTokenContent(request, "api://local.pto.veilarboppfolging/.default")
                    respondOk(underOppfolgingMock)
                }
                "/veilarboppfolging/api/v2/veileder" -> respondOk(veilederMock)
                "/veilarboppfolging/api/person/oppfolgingsenhet" -> respondOk(oppfolgingsenhetMock)
                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }

        withMockOAuth2Server {
            testApplication {
                application {
                    setupEnvironment(this@withMockOAuth2Server)
                    module(Configuration(veilarboppfolgingConfig = Configuration.VeilarboppfolgingConfig(httpClient = veilarboppfolgingMockClient)))
                }
                val response = client.get("/v1/oppfolging/info?aktorId=123")
                Assertions.assertThat(response.status).isEqualTo(HttpStatusCode.Unauthorized)
            }
        }
    }

    @Test
    fun `hent veilederinfo feiler`() {
        val underOppfolgingDTO = UnderOppfolgingDTO(true)
        val underOppfolgingMock = Gson().toJson(underOppfolgingDTO)

        val oppfolgingsenhetDTO = OppfolgingsenhetDTO("NAV Grünerløkka", "1234")
        val oppfolgingsenhetMock = Gson().toJson(oppfolgingsenhetDTO)

        val veilarboppfolgingMockClient = createMockClient { request ->
            when (request.url.encodedPath) {
                "/veilarboppfolging/api/v2/oppfolging" -> {
                    checkBearerTokenContent(request, "api://local.poao.veilarboppfolging/.default")
                    respondOk(underOppfolgingMock)
                }
                "/veilarboppfolging/api/v2/veileder" -> respondBadRequest()
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
                assertEquals(response.status, HttpStatusCode.OK)
                Assertions.assertThat(oppfolgingsinfo.feil).isNotEmpty()
                Assertions.assertThat(oppfolgingsinfo.feil?.get(0)?.feilkilder).isEqualTo("veilederinfo")
            }
        }
    }

    private fun checkBearerTokenContent(request: HttpRequestData, expectedAudience: String) {
        val authString = request.headers[HttpHeaders.Authorization]?.substringAfter("Bearer ")
        val authJwt = JWT.decode(authString)
        Assertions.assertThat(authJwt.subject).isEqualTo("enduser")
        Assertions.assertThat(authJwt.audience).containsExactly(expectedAudience)
    }
}