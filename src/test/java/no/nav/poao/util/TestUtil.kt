package no.nav.poao.util

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*


internal fun createMockClient(block: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): HttpClient {
    return HttpClient(MockEngine) {
        expectSuccess = false

        engine {
            addHandler {
                request -> this.block(request)
            }
        }
    }
}




