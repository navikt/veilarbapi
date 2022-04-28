package no.nav.poao.rest

import com.google.gson.Gson
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.common.types.identer.NavIdent
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
import no.nav.veilarbapi.model.Oppfolgingsperioder
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.kotlin.mock
import org.threeten.bp.OffsetDateTime
import java.util.*
import kotlin.test.assertEquals

class ArbeidsoppfolgingRoutesTest {
    private val veilarbaktivitetConfig =
        Configuration.VeilarbaktivitetConfig(url = "http://localhost:8080/veilarbaktivitet")
    private val veilarbdialogConfig = Configuration.VeilarbdialogConfig(url = "http://localhost:8080/veilarbdialog")
    private val veilarboppfolgingConfig =
        Configuration.VeilarboppfolgingConfig(url = "http://localhost:8080/veilarboppfolging")

    val uuid1 = UUID.randomUUID()
    val uuid2 = UUID.randomUUID()

    init {
        no.nav.veilarbaktivitet.JSON()
        no.nav.veilarbdialog.JSON()
        no.nav.veilarbapi.JSON()
    }

    @Test
    fun testHentPeriodeRoute() {
        val mockOppfolgingService = oppfolgingService()

        withTestApplication({
            arbeidsoppfolgingRoutes(false, mockOppfolgingService)
            configureSerialization()
        }) {
            handleRequest(HttpMethod.Get, "/v1/oppfolging/periode?aktorId=123").apply {
                assertEquals(HttpStatusCode.OK, response.status())

                val oppfolgingsperioder =
                    no.nav.veilarbapi.JSON.deserialize<Oppfolgingsperioder>(
                        response.content,
                        Oppfolgingsperioder::class.java
                    )

                assertThat(oppfolgingsperioder.oppfolgingsperioder).hasSize(2)
                assertThat(oppfolgingsperioder.oppfolgingsperioder!![0].aktiviteter).hasSize(1)
                assertThat(oppfolgingsperioder.oppfolgingsperioder!![0].aktiviteter!![0].behandling.tittel).isEqualTo("ikke kvp")
                assertThat(oppfolgingsperioder.oppfolgingsperioder!![0].aktiviteter!![0].behandling.dialog!!.meldinger!!).hasSize(
                    1
                )
                assertThat(oppfolgingsperioder.oppfolgingsperioder!![1].aktiviteter).hasSize(1)
                assertThat(oppfolgingsperioder.oppfolgingsperioder!![1].aktiviteter!![0].stillingFraNav.dialog).isNull()
            }
        }
    }

    @Test
    fun testHentOppfolgingsinfo() {
        val mockOppfolgingService = oppfolgingService()

        withTestApplication({
            arbeidsoppfolgingRoutes(false, mockOppfolgingService)
            configureSerialization()
        }) {
            handleRequest(HttpMethod.Get, "/v1/oppfolging/info?aktorId=123").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun testTomOppfolgingsenhetSkalReturnere204() {
        val underOppfolgingDTO = UnderOppfolgingDTO(true)
        val underOppfolgingMock = Gson().toJson(underOppfolgingDTO)

        val veilederDTO = VeilederDTO(NavIdent("z123456"))
        val veilederMock = Gson().toJson(veilederDTO)

        val oppfolgingsenhetDTO = OppfolgingsenhetDTO(null, null)
        val oppfolgingsenhetMock = Gson().toJson(oppfolgingsenhetDTO)

        val httpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
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
            }
        }

        val veilarboppfolgingClient = VeilarboppfolgingClientImpl(
            baseUrl = veilarboppfolgingConfig.url,
            veilarboppfolgingTokenProvider = { "VEILARBOPPFOLGING_TOKEN" },
            proxyTokenProvider = { "PROXY_TOKEN" },
            client = httpClient
        )

        val oppfolgingService = OppfolgingService(
            veilarbaktivitetClient = mock {},
            veilarbdialogClient = mock {},
            veilarboppfolgingClient = veilarboppfolgingClient
        )

        withTestApplication({
            arbeidsoppfolgingRoutes(false, oppfolgingService)
            configureSerialization()

        }) {
            handleRequest(HttpMethod.Get, "/v1/oppfolging/info?aktorId=123").apply {
                assertEquals(HttpStatusCode.NoContent, response.status())
            }
        }
    }

    @Test
    fun testExceptionHandler() {
        val veilederDTO = VeilederDTO(NavIdent("z123456"))
        val veilederMock = Gson().toJson(veilederDTO)

        val oppfolgingsenhetDTO = OppfolgingsenhetDTO("NAV Grünerløkka", "1234")
        val oppfolgingsenhetMock = Gson().toJson(oppfolgingsenhetDTO)

        val httpClient = HttpClient(MockEngine) {
            expectSuccess = false
            engine {
                addHandler { request ->
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
            }
        }

        val veilarboppfolgingClient = VeilarboppfolgingClientImpl(
            baseUrl = veilarboppfolgingConfig.url,
            veilarboppfolgingTokenProvider = { "VEILARBOPPFOLGING_TOKEN" },
            proxyTokenProvider = { "PROXY_TOKEN" },
            client = httpClient
        )

        val oppfolgingService = OppfolgingService(
            veilarbaktivitetClient = mock {},
            veilarbdialogClient = mock {},
            veilarboppfolgingClient = veilarboppfolgingClient
        )

        withTestApplication({
            arbeidsoppfolgingRoutes(false, oppfolgingService)
            configureSerialization()
            configureExceptionHandler()
        }) {
            handleRequest(HttpMethod.Get, "/v1/oppfolging/info?aktorId=123").apply {
                assertEquals(HttpStatusCode.Forbidden, response.status())
            }
        }
    }

    private fun oppfolgingService(): OppfolgingService {
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

        val mockAktiviteter = JSON.getGson().toJson(internAktiviteter)
        val mockDialoger = no.nav.veilarbdialog.JSON.getGson().toJson(internDialoger)

        val veilarbaktivitetClient = VeilarbaktivitetClientImpl(
            baseUrl = veilarbaktivitetConfig.url,
            veilarbaktivitetTokenProvider = { "VEILARBAKTIVITET_TOKEN" },
            proxyTokenProvider = { "PROXY_TOKEN" },
            client = createMockClient { respondOk(mockAktiviteter) }
        )

        val veilarbdialogClient = VeilarbdialogClientImpl(
            baseUrl = veilarbdialogConfig.url,
            veilarbdialogTokenProvider = { "VEILARBDIALOG_TOKEN" },
            proxyTokenProvider = { "PROXY_TOKEN" },
            client = createMockClient { respondOk(mockDialoger) }
        )

        val veilarboppfolgingClient = VeilarboppfolgingClientImpl(
            baseUrl = veilarboppfolgingConfig.url,
            veilarboppfolgingTokenProvider = { "VEILARBOPPFOLGING_TOKEN" },
            proxyTokenProvider = { "PROXY_TOKEN" },
            client = createOppfolingMockEngine()
        )

        return OppfolgingService(veilarbaktivitetClient, veilarbdialogClient, veilarboppfolgingClient)
    }

    private fun createOppfolingMockEngine(): HttpClient {
        return HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    when (request.url.encodedPath) {
                        "/veilarboppfolging/api/v2/oppfolging/perioder" ->
                            respond(oppfolgingsperioderMock(), HttpStatusCode.OK)
                        "/veilarboppfolging/api/v2/oppfolging" ->
                            respond(oppfolgingMock(), HttpStatusCode.OK)
                        "/veilarboppfolging/api/v2/veileder" ->
                            respond(veilederMock(), HttpStatusCode.OK)
                        "/veilarboppfolging/api/person/oppfolgingsenhet" ->
                            respond(enhetMock(), HttpStatusCode.OK)
                        else -> error("Unhandled ${request.url.encodedPath}")
                    }
                }
            }
        }
    }

    private fun oppfolgingsperioderMock(): String {
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
        return Gson().toJson(oppfolgingsperiodeDTOer)
    }

    private fun oppfolgingMock(): String {
        val underOppfolging = UnderOppfolgingDTO().apply {
            erUnderOppfolging = true
        }

        return Gson().toJson(underOppfolging)
    }

    private fun veilederMock(): String {
        val veileder = VeilederDTO().apply {
            veilederIdent = NavIdent("z123456")
        }
        return Gson().toJson(veileder)
    }

    private fun enhetMock(): String {
        val enhet = OppfolgingsenhetDTO().apply {
            navn = "NAV Grünerløkka"
            enhetId = "1234"
        }

        return Gson().toJson(enhet)
    }


}