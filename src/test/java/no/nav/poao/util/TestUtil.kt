package no.nav.poao.util

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.server.testing.*

//internal fun createMockClient(block: HttpClientConfig<MockEngineConfig>.() -> Unit = {}): HttpClient {
//    return HttpClient(MockEngine) {
//        expectSuccess = false
//
//        this.apply(block)
//    }
//}

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




