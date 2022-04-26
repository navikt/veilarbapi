package no.nav.poao.util

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.engine.okhttp.*
import io.ktor.http.*
import io.ktor.utils.io.*
import no.nav.poao.veilarbapi.setup.http.baseClient

internal fun createMockClient(httpStatusCode: HttpStatusCode, json: String, block: HttpClientConfig<OkHttpConfig>.() -> Unit = {}): HttpClient {
    val mockEngine = MockEngine {
        respond(
            content = ByteReadChannel(json),
            status = httpStatusCode,
            headers = headersOf(HttpHeaders.ContentType, "application/json")
        )
    }



}



