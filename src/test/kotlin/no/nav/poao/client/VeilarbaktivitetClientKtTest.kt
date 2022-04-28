package no.nav.poao.client

import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.utils.io.*
import kotlinx.coroutines.runBlocking
import no.nav.common.types.identer.AktorId
import no.nav.poao.veilarbapi.aktivitet.VeilarbaktivitetClientImpl
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.poao.veilarbapi.setup.http.baseClient
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import kotlin.test.assertTrue


class VeilarbaktivitetClientKtTest {
    private val veilarbaktivitetConfig = Configuration.VeilarbaktivitetConfig(url = "http://localhost:8080/veilarbaktivitet")

    @Test
    fun testHentAktiviteterWithMockEngine() {
        val mockEngine = MockEngine {
            respond(
                content = ByteReadChannel(mockAktiviteterJson),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = VeilarbaktivitetClientImpl(
            baseUrl = veilarbaktivitetConfig.url,
            veilarbaktivitetTokenProvider = { "VEILARBAKTIVITET_TOKEN" },
            proxyTokenProvider = { "PROXY_TOKEN" },
            client = baseClient(mockEngine)
        )
        runBlocking {
            val aktiviteter = client.hentAktiviteter(AktorId.of("123456789101"), null)
            assertThat(aktiviteter.getOrNull()).hasSize(2)
        }
    }

    @Test
    fun testServerErrorWithMockEngine() {
        val mockEngine = MockEngine {
            respond(
                content = ByteReadChannel(mockServerError),
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = VeilarbaktivitetClientImpl(
            baseUrl = veilarbaktivitetConfig.url,
            veilarbaktivitetTokenProvider = { "VEILARBAKTIVITET_TOKEN" },
            proxyTokenProvider = { "PROXY_TOKEN" },
            client = baseClient(mockEngine)
        )
        runBlocking {
            val hentAktiviteter = client.hentAktiviteter(AktorId.of("123456789101"), null)
            assertTrue(hentAktiviteter.isFailure)
        }
    }

    val mockAktiviteterJson = """
        [
          {
            "aktivitet_type": "mote",
            "avtalt_med_nav": false,
            "status": "PLANLAGT",
            "beskrivelse": "beskrivelse",
            "tittel": "tittel",
            "fra_dato": "1971-11-21T20:50:47Z",
            "til_dato": "1920-12-16T05:07:19Z",
            "opprettet_dato": "2022-02-11T13:28:42.672Z",
            "endret_dato": "2022-02-11T13:28:42.672Z",
            "adresse": "en adresse",
            "forberedelser": "en forbedredelse",
            "kanal": "OPPMOTE",
            "referat": "et referat",
            "referatPublisert": true
          },
          {
            "aktivitet_type": "egenaktivitet",
            "avtalt_med_nav": false,
            "status": "PLANLAGT",
            "beskrivelse": "beskrivelse",
            "tittel": "tittel",
            "fra_dato": "1984-11-07T04:27:16Z",
            "til_dato": "1984-02-14T09:05:14Z",
            "opprettet_dato": "2022-02-11T13:28:45.184Z",
            "endret_dato": "2022-02-11T13:28:45.184Z",
            "hensikt": "nada",
            "oppfolging": "oppfølging"
          }
        ]""".trimIndent()

    val mockServerError = """
        {
          "timestamp": "2022-02-17T13:19:42.774+00:00",
          "status": 500,
          "error": "Internal Server Error",
          "trace": "java.lang.NullPointerException: Name is null\n\tat java.base/java.lang.Enum.valueOf(Enum.java:271)\n\tat no.nav.common.auth.context.UserRole.valueOf(UserRole.java:3)\n\tat no.nav.veilarbaktivitet.config.TestAuthContextFilter.doFilter(TestAuthContextFilter.java:24)\n\tat org.apache.catalina.core.ApplicationFilterChain.internalDoFilter(ApplicationFilterChain.java:189)\n\tat org.apache.catalina.core.ApplicationFilterChain.doFilter(ApplicationFilterChain.java:162)\n...",
          "message": "Name is null",
          "path": "/veilarbaktivitet/internal/api/v1/aktivitet/520"
        }
    """.trimIndent()
}