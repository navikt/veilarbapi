package no.nav.poao.rest

import com.google.gson.Gson
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.poao.util.InternAktivitetBuilder
import no.nav.poao.util.InternDialogBuilder
import no.nav.poao.util.createMockClient
import no.nav.poao.veilarbapi.aktivitet.VeilarbaktivitetClientImpl
import no.nav.poao.veilarbapi.dialog.VeilarbdialogClientImpl
import no.nav.poao.veilarbapi.oppfolging.*
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.poao.veilarbapi.setup.plugins.configureExceptionHandler
import no.nav.poao.veilarbapi.setup.plugins.configureSerialization
import no.nav.poao.veilarbapi.setup.rest.arbeidsoppfolgingRoutes
import no.nav.veilarbaktivitet.JSON
import no.nav.veilarbapi.model.Oppfolgingsinfo
import no.nav.veilarbapi.model.Oppfolgingsperioder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.junit.Test
import org.mockito.kotlin.mock
import org.threeten.bp.OffsetDateTime
import java.util.*
import kotlin.test.assertEquals

class ArbeidsoppfolgingRoutesTest {
    private val veilarbaktivitetConfig =
        Configuration.VeilarbaktivitetConfig(url = "/veilarbaktivitet")
    private val veilarbdialogConfig = Configuration.VeilarbdialogConfig(url = "/veilarbdialog")
    private val veilarboppfolgingConfig =
        Configuration.VeilarboppfolgingConfig(url = "/veilarboppfolging")

    init {
        no.nav.veilarbaktivitet.JSON()
        no.nav.veilarbdialog.JSON()
        no.nav.veilarbapi.JSON()
    }

    @Test
    fun `happy case hent perioder`() {
        val mockOppfolgingService = oppfolgingService()

        testApplication {
            application {
                arbeidsoppfolgingRoutes(false, mockOppfolgingService)
                configureSerialization()
            }
            val response = client.get("/v1/oppfolging/periode?aktorId=123")
            assertEquals(HttpStatusCode.OK, response.status)

            val oppfolgingsperioder =
                no.nav.veilarbapi.JSON.deserialize<Oppfolgingsperioder>(
                    response.bodyAsText(),
                    Oppfolgingsperioder::class.java
                )
            assertThat(oppfolgingsperioder.oppfolgingsperioder).hasSize(2)
            assertThat(oppfolgingsperioder.oppfolgingsperioder!![0].aktiviteter).hasSize(1)
            assertThat(oppfolgingsperioder.oppfolgingsperioder!![0].aktiviteter!![0].behandling.tittel).isEqualTo("ikke kvp")
            assertThat(oppfolgingsperioder.oppfolgingsperioder!![0].aktiviteter!![0].behandling.dialog!!.meldinger!!).hasSize(1)
            assertThat(oppfolgingsperioder.oppfolgingsperioder!![1].aktiviteter).hasSize(1)
            assertThat(oppfolgingsperioder.oppfolgingsperioder!![1].aktiviteter!![0].stillingFraNav.dialog).isNull()
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
            val response = client.get("/v1/oppfolging/periode?aktorId=123")
            assertEquals(HttpStatusCode.OK, response.status)

            val oppfolgingsperioder =
                no.nav.veilarbapi.JSON.deserialize<Oppfolgingsperioder>(
                    response.bodyAsText(),
                    Oppfolgingsperioder::class.java
                )

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
        val underOppfolgingMock = Gson().toJson(underOppfolgingDTO)

        val veilederDTO = VeilederDTO("z123456")
        val veilederMock = Gson().toJson(veilederDTO)

        val oppfolgingsenhetDTO = OppfolgingsenhetDTO(null, null)
        val oppfolgingsenhetMock = Gson().toJson(oppfolgingsenhetDTO)

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
            val response = client.get("/v1/oppfolging/info?aktorId=123")
            assertEquals(HttpStatusCode.NoContent, response.status)
        }
    }

    @Test
    fun `test oppfolgingsinfo feilhaandtering`() {
        val veilederDTO = VeilederDTO("z123456")
        val veilederMock = Gson().toJson(veilederDTO)

        val oppfolgingsenhetDTO = OppfolgingsenhetDTO("NAV Grünerløkka", "1234")
        val oppfolgingsenhetMock = Gson().toJson(oppfolgingsenhetDTO)

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
            val response = client.get("/v1/oppfolging/info?aktorId=123")
            assertEquals(HttpStatusCode.Forbidden, response.status)
        }

        val underOppfolgingDTO = UnderOppfolgingDTO(true)
        val underOppfolgingMock = Gson().toJson(underOppfolgingDTO)

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
            val response = client.get("/v1/oppfolging/info?aktorId=123")
            assertEquals(HttpStatusCode.OK, response.status)

            val oppfolgingsinfo: Oppfolgingsinfo = no.nav.veilarbapi.JSON.deserialize(
                response.bodyAsText(),
                Oppfolgingsinfo::class.java
            )

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
            InternAktivitetBuilder.nyAktivitet("egenaktivitet").oppfolgingsperiodeId(uuid1),
            InternAktivitetBuilder.nyAktivitet("behandling").oppfolgingsperiodeId(uuid1).aktivitetId("3")
                .tittel("ikke kvp"),
            InternAktivitetBuilder.nyAktivitet("sokeavtale", true).oppfolgingsperiodeId(uuid1).aktivitetId("6"),
            InternAktivitetBuilder.nyAktivitet("stilling_fra_nav").oppfolgingsperiodeId(uuid2).aktivitetId("9"),
        )

        val internDialog = InternDialogBuilder.nyDialog().oppfolgingsperiodeId(uuid1).aktivitetId("3")
        InternDialogBuilder.nyHenvendelsePaaDialog(internDialog, true)
        val internDialog2 = InternDialogBuilder.nyDialog(true, true).oppfolgingsperiodeId(uuid2).aktivitetId("9")
        InternDialogBuilder.nyHenvendelsePaaDialog(internDialog2, false)
        val internDialoger = listOf(
            internDialog,
            InternDialogBuilder.nyDialog().oppfolgingsperiodeId(uuid1).aktivitetId("6"),
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

        val mockAktiviteter = JSON.getGson().toJson(internAktiviteter)
        val mockDialoger = no.nav.veilarbdialog.JSON.getGson().toJson(internDialoger)
        val mockOppfolgingsperioder = gson().toJson(oppfolgingsperiodeDTOer)

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
