package no.nav.poao.rest

import com.auth0.jwt.JWT
import com.expediagroup.graphql.client.types.GraphQLClientError
import com.nimbusds.jwt.SignedJWT
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import no.nav.poao.util.*
import no.nav.poao.util.setupEnvironment
import no.nav.poao.util.withWiremockServer
import no.nav.poao.veilarbapi.module
import no.nav.poao.veilarbapi.oppfolging.*
import no.nav.poao.veilarbapi.oppfolging.serdes.VeilarbapiSerializerModule
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.security.mock.oauth2.withMockOAuth2Server
import no.nav.veilarbapi.model.Oppfolgingsinfo
import org.assertj.core.api.Assertions
import org.junit.Test
import java.time.OffsetDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class ArbeidsoppfolgingRoutesITest {

    @Test
    fun `hent oppfolgingsinfo med wiremock for eksterne kall`() = runTest {
        withWiremockServer {
            withMockOAuth2Server {
                testApplication {
                    application {
                        setupEnvironment(this@withMockOAuth2Server, this@withWiremockServer)
                        module()
                    }

                    val underOppfolgingDTO = UnderOppfolgingDTO(true)
                    val veilederDTO = VeilederDTO("z999999")
                    val oppfolgingsenhetDTO = OppfolgingsenhetDTO(navn = "Nav Grunerløkka", "1234")

                    stubOppfolgingsInfo(this@withWiremockServer, oppfolgingsenhetDTO, veilederDTO.veilederIdent!!)

                    val token = this@withMockOAuth2Server.issueToken(subject = "enduser", audience = "client_id")
                    val response = client.get("/v1/oppfolging/info?aktorId=123") {
                        header(HttpHeaders.Authorization, "Bearer ${token.serialize()}")
                    }
                    assertEquals(HttpStatusCode.OK, response.status)

                    val oppfolgingsinfo = Json.decodeFromString<Oppfolgingsinfo>(response.bodyAsText())
                    Assertions.assertThat(oppfolgingsinfo.underOppfolging)
                        .isEqualTo(underOppfolgingDTO.erUnderOppfolging)
                    Assertions.assertThat(oppfolgingsinfo.primaerVeileder)
                        .isEqualTo(veilederDTO.veilederIdent)
                    Assertions.assertThat(oppfolgingsinfo.oppfolgingsEnhet?.enhetId)
                        .isEqualTo(oppfolgingsenhetDTO.enhetId)
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
        val underOppfolgingMock = Json.encodeToString(underOppfolgingDTO)

        val veilederDTO = VeilederDTO("z123456")
        val veilederMock = Json.encodeToString(veilederDTO)

        val oppfolgingsenhetDTO = OppfolgingsenhetDTO("NAV Grünerløkka", "1234")
        val oppfolgingsenhetMock = Json.encodeToString(oppfolgingsenhetDTO)

        val veilarboppfolgingMockClient = createMockClient { request ->
            when (request.url.encodedPath) {
                "/veilarboppfolging/api/graphql" -> {
                    checkBearerTokenContent(request, "api://local.poao.veilarboppfolging/.default")
                    respondOk(oppfolgingsInfoResponse(oppfolgingsenhetDTO, veilederDTO.veilederIdent!!))
                }
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
                val oppfolgingsinfo = Json.decodeFromString<Oppfolgingsinfo>(
                        response.bodyAsText()
                    )
                assertEquals(underOppfolgingDTO.erUnderOppfolging, oppfolgingsinfo.underOppfolging)
            }
        }
    }

    @Test
    fun `hent oppfolgingsperiode med mockengine client for eksterne tjenester`() {
        val json = Json { serializersModule = VeilarbapiSerializerModule }
        val uuid = UUID.randomUUID()
        val oppfolgingsperiode = listOf(OppfolgingsperiodeDTO(uuid, "aktorid", null, OffsetDateTime.now().minusDays(1), null))
        val internAktiviteter = listOf(InternAktivitetBuilder.nyEgenaktivitet().copy(oppfolgingsperiodeId = uuid, aktivitetId = "3"))
        val internDialoger = listOf(InternDialogBuilder.nyDialog().copy(oppfolgingsperiodeId = uuid, aktivitetId = "3"))

        val mockOppfolgingsperiode = json.encodeToString(oppfolgingsperiode)
        val mockAktiviteter = json.encodeToString(internAktiviteter)
        val mockDialoger = json.encodeToString(internDialoger)

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
                val body = response.bodyAsText()
                assertNotNull(body)
                assertEquals(HttpStatusCode.OK, response.status)
            }
        }
    }

    @Test
    fun `kall uten auth token skal feile med 401`() {
        val underOppfolgingDTO = UnderOppfolgingDTO(true)
        val underOppfolgingMock = Json.encodeToString(underOppfolgingDTO)

        val veilederDTO = VeilederDTO("z123456")
        val veilederMock = Json.encodeToString(veilederDTO)

        val oppfolgingsenhetDTO = OppfolgingsenhetDTO("NAV Grünerløkka", "1234")
        val oppfolgingsenhetMock = Json.encodeToString(oppfolgingsenhetDTO)

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
        val oppfolgingsenhetDTO = OppfolgingsenhetDTO("NAV Grünerløkka", "1234")

        val veilarboppfolgingMockClient = createMockClient { request ->
            when (request.url.encodedPath) {
                "/veilarboppfolging/api/graphql" -> {
                    checkBearerTokenContent(request, "api://local.poao.veilarboppfolging/.default")
                    // Alt untatt veileder funker
                    respondOk(oppfolgingsInfoResponse(
                        oppfolgingsenhetDTO,
                        null,
                        errors = listOf(
                            object: GraphQLClientError {
                                override val message: String
                                    get() = "WOOPS"
                                override val path: List<Any>?
                                    get() = listOf("veilederinfo")
                            }
                        )
                    ))
                }
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
                val oppfolgingsinfo = Json.decodeFromString<Oppfolgingsinfo>(response.bodyAsText())
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