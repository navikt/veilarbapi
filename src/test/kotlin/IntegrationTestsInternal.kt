import com.github.tomakehurst.wiremock.http.HttpClientFactory.createClient
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.testApplication
import kotlinx.coroutines.runBlocking
import no.nav.poao.util.RealServerTestUtil.Companion.setDefaultTestSystemProperties
import no.nav.poao.util.RealServerTestUtil.Companion.withMockOAuth2ServerWithEnv
import no.nav.poao.veilarbapi.module
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test


class IntegrationTestsInternal {

    @Test
    fun testIsAlive() = testApplication {
        setDefaultTestSystemProperties()
        withMockOAuth2ServerWithEnv {
            application {
                val config = no.nav.poao.veilarbapi.setup.config.Configuration()
                module(config)
            }

            val client = createClient {}

            client.get("/internal/isAlive")
                .let { response ->
                    assertThat(response.status).isEqualTo(HttpStatusCode.OK)
                    assertThat(response.bodyAsText()).isEmpty()
                }

            client.get("/internal/isReady")
                .let { response ->
                    assertThat(response.status).isEqualTo(HttpStatusCode.OK)
                    assertThat(response.bodyAsText()).isEmpty()
                }
        }
    }
}
