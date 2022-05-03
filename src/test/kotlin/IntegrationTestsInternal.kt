import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.poao.util.RealServerTestUtil
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test


class IntegrationTestsInternal {

    private companion object {
        init {
            RealServerTestUtil.setup()
        }
    }

    @Test
    fun testIsAlive() {
        val client = HttpClient(OkHttp)
        runBlocking {
            val response: HttpResponse = client.get("http://0.0.0.0:8080/internal/isAlive")
            val responseString = response.bodyAsText()
            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(responseString).isEmpty()
        }
    }

    @Test
    fun testIsReady() {
        val client = HttpClient(OkHttp)
        runBlocking {
            val response: HttpResponse = client.get("http://0.0.0.0:8080/internal/isReady")
            val responseString = response.bodyAsText()
            assertThat(response.status).isEqualTo(HttpStatusCode.OK)
            assertThat(responseString).isEmpty()
        }
    }
}
