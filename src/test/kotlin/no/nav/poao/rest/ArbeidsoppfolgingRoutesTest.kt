package no.nav.poao.rest

import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import no.nav.poao.util.InternAktivitetBuilder
import no.nav.poao.util.InternDialogBuilder
import no.nav.poao.util.createMockClient
import no.nav.poao.util.nyHenvendelsePaaDialog
import no.nav.poao.veilarbapi.aktivitet.VeilarbaktivitetClientImpl
import no.nav.poao.veilarbapi.dialog.VeilarbdialogClientImpl
import no.nav.poao.veilarbapi.oppfolging.*
import no.nav.poao.veilarbapi.oppfolging.serdes.VeilarbapiSerializerModule
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.poao.veilarbapi.setup.plugins.configureExceptionHandler
import no.nav.poao.veilarbapi.setup.plugins.configureSerialization
import no.nav.poao.veilarbapi.setup.rest.arbeidsoppfolgingRoutes
import no.nav.veilarbapi.model.Oppfolgingsinfo
import no.nav.veilarbapi.model.Oppfolgingsperioder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.mockito.kotlin.mock
import java.time.OffsetDateTime
import java.util.*
import kotlin.test.assertEquals

val mockJwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpZCI6MTIzNDU2Nzg5LCJuYW1lIjoiSm9zZXBoIn0.OpOSSw7e485LOP5PrzScxHb7SR6sAOMRckfFwi4rp7o"

class ArbeidsoppfolgingRoutesTest {
    private val veilarbaktivitetConfig =
        Configuration.VeilarbaktivitetConfig(url = "/veilarbaktivitet")
    private val veilarbdialogConfig = Configuration.VeilarbdialogConfig(url = "/veilarbdialog")
    private val veilarboppfolgingConfig =
        Configuration.VeilarboppfolgingConfig(url = "/veilarboppfolging")

    val json = Json {
        serializersModule = VeilarbapiSerializerModule
    }

    @Test
    fun `happy case hent perioder`() {
        val mockOppfolgingService = oppfolgingService()

        testApplication {
            application {
                arbeidsoppfolgingRoutes(false, mockOppfolgingService)
                configureSerialization()
            }
            val response = client.get("/v1/oppfolging/periode?aktorId=123") {
                header("Authorization", "Bearer $mockJwtToken")
            }
            val responsebody = response.bodyAsText()
            assertEquals(HttpStatusCode.OK, response.status)

            val oppfolgingsperioder = json.decodeFromString<Oppfolgingsperioder>(responsebody)
            assertThat(oppfolgingsperioder.oppfolgingsperioder).hasSize(2).withFailMessage { "Forventet 2 oppfolgingsperioder men fant ikke det" }
            assertThat(oppfolgingsperioder.oppfolgingsperioder!![0].aktiviteter).hasSize(3).withFailMessage { "Forventet 3 aktiviteter i periode 1" }

            val behandling = oppfolgingsperioder.oppfolgingsperioder[0].aktiviteter!![1]
            val egenaktivitet = oppfolgingsperioder.oppfolgingsperioder[0].aktiviteter!![0]

            assertThat(behandling.tittel).isEqualTo("ikke kvp")
            assertThat(behandling.dialog!!.meldinger!!).hasSize(1)
            assertThat(egenaktivitet.tittel).isEqualTo("tittel")
            assertThat(egenaktivitet.dialog).isNull()
            assertThat(oppfolgingsperioder.oppfolgingsperioder[1].aktiviteter).hasSize(1)
            assertThat(oppfolgingsperioder.oppfolgingsperioder[1].aktiviteter!![0].dialog).isNull()
        }
    }

    @Test
    fun `test hentperioder feilhaandtering `() {
        val veilarbaktivitetClient = VeilarbaktivitetClientImpl(
            baseUrl = veilarbaktivitetConfig.url,
            veilarbaktivitetTokenProvider = { "VEILARBAKTIVITET_TOKEN" },
            client = createMockClient { respondError(HttpStatusCode.Forbidden) }
        )

        val veilarbdialogClient = VeilarbdialogClientImpl(
            baseUrl = veilarbdialogConfig.url,
            veilarbdialogTokenProvider = { "VEILARBDIALOG_TOKEN" },
            client = createMockClient { respondError(HttpStatusCode.Forbidden) }
        )

        val veilarboppfolgingClient = VeilarboppfolgingClientImpl(
            baseUrl = veilarboppfolgingConfig.url,
            veilarboppfolgingTokenProvider = { "VEILARBOPPFOLGING_TOKEN" },
            client = createMockClient { respondError(HttpStatusCode.Forbidden) }
        )

        val oppfolgingService = OppfolgingService(veilarbaktivitetClient, veilarbdialogClient, veilarboppfolgingClient)

        testApplication {
            application {
                arbeidsoppfolgingRoutes(false, oppfolgingService)
                configureSerialization()
            }
            val response = client.get("/v1/oppfolging/periode?aktorId=123") {
                header("Authorization", "Bearer $mockJwtToken")
            }
            assertEquals(HttpStatusCode.OK, response.status)

            val oppfolgingsperioder = Json.decodeFromString<Oppfolgingsperioder>(response.bodyAsText())

            SoftAssertions().apply {
                assertThat(oppfolgingsperioder.oppfolgingsperioder).isEmpty()
                assertThat(oppfolgingsperioder.feil).hasSize(3)
                assertThat(oppfolgingsperioder.feil!![0].feilmelding).isEqualTo("Mangler tilgang")
            }.assertAll()
        }
    }

    @Test
    fun `tom oppfolgingsenhet skal returnere 204`() {
        val underOppfolgingDTO = UnderOppfolgingDTO(true)
        val underOppfolgingMock = Json.encodeToString(underOppfolgingDTO)

        val veilederDTO = VeilederDTO("z123456")
        val veilederMock = Json.encodeToString(veilederDTO)

        val oppfolgingsenhetDTO = OppfolgingsenhetDTO(null, null)
        val oppfolgingsenhetMock = Json.encodeToString(oppfolgingsenhetDTO)

        val httpClient = createMockClient { request ->
            when (request.url.encodedPath) {
                "/veilarboppfolging/api/v2/oppfolging" ->
                    respond(underOppfolgingMock, HttpStatusCode.OK)
                "/veilarboppfolging/api/v2/veileder" ->
                    respond(veilederMock, HttpStatusCode.OK)
                "/veilarboppfolging/api/person/oppfolgingsenhet" ->
                    respond(oppfolgingsenhetMock, HttpStatusCode.OK)
                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }

        val veilarboppfolgingClient = VeilarboppfolgingClientImpl(
            baseUrl = veilarboppfolgingConfig.url,
            veilarboppfolgingTokenProvider = { "VEILARBOPPFOLGING_TOKEN" },
            client = httpClient
        )

        val oppfolgingService = OppfolgingService(
            veilarbaktivitetClient = mock {},
            veilarbdialogClient = mock {},
            veilarboppfolgingClient = veilarboppfolgingClient
        )

        testApplication {
            application {
                arbeidsoppfolgingRoutes(false, oppfolgingService)
                configureSerialization()
            }
            val response = client.get("/v1/oppfolging/info?aktorId=123") {
                header("Authorization", "Bearer $mockJwtToken")
            }
            assertEquals(HttpStatusCode.NoContent, response.status)
        }
    }

    @Test
    fun `test oppfolgingsinfo feilhaandtering`() {
        val veilederDTO = VeilederDTO("z123456")
        val veilederMock = Json.encodeToString(veilederDTO)

        val oppfolgingsenhetDTO = OppfolgingsenhetDTO("NAV Grünerløkka", "1234")
        val oppfolgingsenhetMock = Json.encodeToString(oppfolgingsenhetDTO)

        val veilarboppfolgingHttpClient = createMockClient { request ->
            when (request.url.encodedPath) {
                "/veilarboppfolging/api/v2/oppfolging" ->
                    respondError(HttpStatusCode.Forbidden)
                "/veilarboppfolging/api/v2/veileder" ->
                    respond(veilederMock, HttpStatusCode.OK)
                "/veilarboppfolging/api/person/oppfolgingsenhet" ->
                    respond(oppfolgingsenhetMock, HttpStatusCode.OK)
                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }

        val veilarboppfolgingClient = VeilarboppfolgingClientImpl(
            baseUrl = veilarboppfolgingConfig.url,
            veilarboppfolgingTokenProvider = { "VEILARBOPPFOLGING_TOKEN" },
            client = veilarboppfolgingHttpClient
        )

        val oppfolgingService = OppfolgingService(
            veilarbaktivitetClient = mock {},
            veilarbdialogClient = mock {},
            veilarboppfolgingClient = veilarboppfolgingClient
        )

        testApplication {
            application {
                arbeidsoppfolgingRoutes(false, oppfolgingService)
                configureSerialization()
                configureExceptionHandler()
            }
            val response = client.get("/v1/oppfolging/info?aktorId=123") {
                header("Authorization", "Bearer $mockJwtToken")
            }
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

        val underOppfolgingDTO = UnderOppfolgingDTO(true)
        val underOppfolgingMock = Json.encodeToString(underOppfolgingDTO)

        val veilarboppfolgingHttpClient2 = createMockClient { request ->
            when (request.url.encodedPath) {
                "/veilarboppfolging/api/v2/oppfolging" ->
                    respondOk(underOppfolgingMock)
                "/veilarboppfolging/api/v2/veileder" ->
                    respondError(HttpStatusCode.Forbidden)
                "/veilarboppfolging/api/person/oppfolgingsenhet" ->
                    respondError(HttpStatusCode.InternalServerError)
                else -> error("Unhandled ${request.url.encodedPath}")
            }
        }

        val veilarboppfolgingClient2 = VeilarboppfolgingClientImpl(
            baseUrl = veilarboppfolgingConfig.url,
            veilarboppfolgingTokenProvider = { "VEILARBOPPFOLGING_TOKEN" },
            client = veilarboppfolgingHttpClient2
        )

        val oppfolgingService2 = OppfolgingService(
            veilarbaktivitetClient = mock {},
            veilarbdialogClient = mock {},
            veilarboppfolgingClient = veilarboppfolgingClient2
        )

        testApplication {
            application {
                arbeidsoppfolgingRoutes(false, oppfolgingService2)
                configureSerialization()
                configureExceptionHandler()
            }
            val response = client.get("/v1/oppfolging/info?aktorId=123") {
                header("Authorization", "Bearer $mockJwtToken")
            }
            assertEquals(HttpStatusCode.OK, response.status)

            val oppfolgingsinfo: Oppfolgingsinfo = Json.decodeFromString<Oppfolgingsinfo>(response.bodyAsText())

            SoftAssertions().apply {
                assertThat(oppfolgingsinfo.underOppfolging).isEqualTo(true)
                assertThat(oppfolgingsinfo.feil).hasSize(2)
                assertThat(oppfolgingsinfo.feil?.find { it.feilkilder == "veilederinfo" }?.feilmelding).isEqualTo("Mangler tilgang")
                assertThat(oppfolgingsinfo.feil?.find { it.feilkilder == "oppfolgingsenhet" }?.feilmelding).isEqualTo("Serverfeil i klientkall")
            }.assertAll()
        }
    }

    private fun oppfolgingService(): OppfolgingService {
        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()

        val internAktiviteter = listOf(
            InternAktivitetBuilder.nyEgenaktivitet().copy(oppfolgingsperiodeId = uuid1),
            InternAktivitetBuilder.nyBehandling().copy(oppfolgingsperiodeId = uuid1, aktivitetId = "3", tittel = "ikke kvp"),
            InternAktivitetBuilder.nySokeavtale().copy(oppfolgingsperiodeId = uuid1, aktivitetId = "6"),
            InternAktivitetBuilder.nyStillingFraNav().copy(oppfolgingsperiodeId = uuid2, aktivitetId = "9"),
        )

        val internDialog = InternDialogBuilder.nyDialog().copy(oppfolgingsperiodeId = uuid1, aktivitetId = "3")
            .nyHenvendelsePaaDialog( true)
        val internDialog2 = InternDialogBuilder.nyDialog(true, true)
            .copy(oppfolgingsperiodeId = uuid2, aktivitetId = "9")
            .nyHenvendelsePaaDialog( false)
        val internDialoger = listOf(
            internDialog,
            InternDialogBuilder.nyDialog().copy(oppfolgingsperiodeId = uuid1, aktivitetId = "6"),
            internDialog2
        )
        val oppfolgingsperiodeDTOer = listOf(
            OppfolgingsperiodeDTO(
                uuid1,
                "aktorid",
                null,
                OffsetDateTime.now().minusDays(4),
                OffsetDateTime.now().minusDays(2)
            ),
            OppfolgingsperiodeDTO(uuid2, "aktorid", null, OffsetDateTime.now().minusDays(1), null)
        )

        val mockAktiviteter = json.encodeToString(internAktiviteter)
        val mockDialoger = json.encodeToString(internDialoger)
        val mockOppfolgingsperioder = json.encodeToString(oppfolgingsperiodeDTOer)

        val veilarbaktivitetClient = VeilarbaktivitetClientImpl(
            baseUrl = veilarbaktivitetConfig.url,
            veilarbaktivitetTokenProvider = { "VEILARBAKTIVITET_TOKEN" },
            client = createMockClient { respondOk(mockAktiviteter) }
        )

        val veilarbdialogClient = VeilarbdialogClientImpl(
            baseUrl = veilarbdialogConfig.url,
            veilarbdialogTokenProvider = { "VEILARBDIALOG_TOKEN" },
            client = createMockClient { respondOk(mockDialoger) }
        )

        val veilarboppfolgingClient = VeilarboppfolgingClientImpl(
            baseUrl = veilarboppfolgingConfig.url,
            veilarboppfolgingTokenProvider = { "VEILARBOPPFOLGING_TOKEN" },
            client = createMockClient { respondOk(mockOppfolgingsperioder) }
        )

        return OppfolgingService(veilarbaktivitetClient, veilarbdialogClient, veilarboppfolgingClient)
    }
}
