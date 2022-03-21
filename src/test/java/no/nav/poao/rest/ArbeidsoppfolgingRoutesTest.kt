package no.nav.poao.rest

import no.nav.poao.veilarbapi.oppfolging.OppfolgingsperiodeDTO
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.server.testing.*
import io.ktor.utils.io.*
import no.nav.poao.util.InternAktivitetBuilder
import no.nav.poao.util.InternDialogBuilder
import no.nav.poao.veilarbapi.aktivitet.VeilarbaktivitetClientImpl
import no.nav.poao.veilarbapi.dialog.VeilarbdialogClientImpl
import no.nav.poao.veilarbapi.oppfolging.OppfolgingService
import no.nav.poao.veilarbapi.oppfolging.VeilarboppfolgingClientImpl
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.poao.veilarbapi.setup.http.baseClient
import no.nav.poao.veilarbapi.setup.plugins.configureSerialization
import no.nav.poao.veilarbapi.setup.rest.arbeidsoppfolgingRoutes
import no.nav.veilarbapi.model.Oppfolgingsperioder
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.threeten.bp.OffsetDateTime
import java.util.*
import kotlin.test.assertEquals

class ArbeidsoppfolgingRoutesTest {
    private val veilarbaktivitetConfig = Configuration.VeilarbaktivitetConfig(url = "http://localhost:8080/veilarbaktivitet")
    private val veilarbdialogConfig = Configuration.VeilarbdialogConfig(url = "http://localhost:8080/veilarbdialog")
    private val veilarboppfolgingConfig = Configuration.VeilarboppfolgingConfig(url = "http://localhost:8080/veilarbaktivitet")

    init {
        no.nav.veilarbaktivitet.JSON()
        no.nav.veilarbdialog.JSON()
        no.nav.veilarbapi.JSON()
    }

    @Test
    fun testHentPeriodeRoute() { // todo - sett opp mockoauth2server
        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()

        val oppfolgingsperiodeDTOer = listOf(
            OppfolgingsperiodeDTO(uuid1, "aktorid", null, OffsetDateTime.now().minusDays(4), OffsetDateTime.now().minusDays(2)),
            OppfolgingsperiodeDTO(uuid2, "aktorid", null, OffsetDateTime.now().minusDays(1), null)
        )

        val internAktiviteter = listOf(
            InternAktivitetBuilder.nyAktivitet("egenaktivitet").oppfolgingsperiodeId(uuid1).aktivitetId("3").tittel("ikke kvp"),
            InternAktivitetBuilder.nyAktivitet("ijobb", true).oppfolgingsperiodeId(uuid1).aktivitetId("6"),
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

        val mockAktiviteter = no.nav.veilarbaktivitet.JSON.getGson().toJson(internAktiviteter)
        val mockDialoger = no.nav.veilarbdialog.JSON.getGson().toJson(internDialoger)
        val mockOppfolgingsperioder = no.nav.veilarbapi.JSON.getGson().toJson(oppfolgingsperiodeDTOer)

        val veilarbaktivitetClient = VeilarbaktivitetClientImpl(
            veilarbaktivitetConfig = veilarbaktivitetConfig,
            veilarbaktivitetTokenProvider = { "VEILARBAKTIVITET_TOKEN" },
            proxyTokenProvider = { "PROXY_TOKEN" },
            client = baseClient(createMockEngine(mockAktiviteter))
        )

        val veilarbdialogClient = VeilarbdialogClientImpl(
            veilarbdialogConfig = veilarbdialogConfig,
            veilarbdialogTokenProvider = { "VEILARBDIALOG_TOKEN" },
            proxyTokenProvider = { "PROXY_TOKEN" },
            client = baseClient(createMockEngine(mockDialoger))
        )

        val veilarboppfolgingClient = VeilarboppfolgingClientImpl(
            veilarboppfolgingConfig = veilarboppfolgingConfig,
            veilarboppfolgingTokenProvider = { "VEILARBOPPFOLGING_TOKEN" },
            proxyTokenProvider = { "PROXY_TOKEN" },
            client = baseClient(createMockEngine(mockOppfolgingsperioder))
        )

        val mockOppfolgingService = OppfolgingService(veilarbaktivitetClient, veilarbdialogClient, veilarboppfolgingClient)

        withTestApplication({
            arbeidsoppfolgingRoutes(false, mockOppfolgingService)
            configureSerialization()
        }) {
            handleRequest(HttpMethod.Get, "/v1/oppfolging/periode?aktorId=123").apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val oppfolgingsperioder =
                    no.nav.veilarbapi.JSON.deserialize<Oppfolgingsperioder>(response.content, Oppfolgingsperioder::class.java)

                assertThat(oppfolgingsperioder.oppfolgingsperioder).hasSize(2)
                assertThat(oppfolgingsperioder.oppfolgingsperioder!![0].aktiviteter).hasSize(1)
                assertThat(oppfolgingsperioder.oppfolgingsperioder!![0].aktiviteter!![0].egenaktivitet.tittel).isEqualTo("ikke kvp")
                assertThat(oppfolgingsperioder.oppfolgingsperioder!![0].aktiviteter!![0].egenaktivitet.dialog!!.meldinger!!).hasSize(1)
                assertThat(oppfolgingsperioder.oppfolgingsperioder!![1].aktiviteter).hasSize(1)
                assertThat(oppfolgingsperioder.oppfolgingsperioder!![1].aktiviteter!![0].stillingFraNav.dialog).isNull()
            }
        }
    }

    private fun createMockEngine(json: String): MockEngine {
        return MockEngine {
            respond(
                content = ByteReadChannel(json),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
    }
}
