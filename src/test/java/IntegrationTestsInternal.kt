import io.ktor.server.engine.*
import io.ktor.server.testing.*
import no.nav.poao.mainTest
import org.apache.http.client.methods.HttpGet
import org.apache.http.client.methods.HttpUriRequest
import org.apache.http.impl.client.BasicResponseHandler
import org.apache.http.impl.client.HttpClientBuilder
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import kotlin.test.assertTrue


class IntegrationTestsInternal {
    lateinit var applicationEngine: ApplicationEngine;

    @Test
    fun testIsAlive() {
        applicationEngine = mainTest()
        withApplication { applicationEngine }
        val request: HttpUriRequest = HttpGet("http://0.0.0.0:8080/internal/isAlive")
        val httpResponse = HttpClientBuilder.create().build().execute(request)
        val responseString = BasicResponseHandler().handleResponse(httpResponse)
        assertTrue(httpResponse.statusLine.statusCode == 200)
        assertThat(responseString).isEmpty()
    }

    @Test
    fun testIsReady() {
        val request: HttpUriRequest = HttpGet("http://0.0.0.0:8080/internal/isReady")
        val httpResponse = HttpClientBuilder.create().build().execute(request)
        val responseString = BasicResponseHandler().handleResponse(httpResponse)
        assertTrue(httpResponse.statusLine.statusCode == 200)
        assertThat(responseString).isEmpty()
    }


}
