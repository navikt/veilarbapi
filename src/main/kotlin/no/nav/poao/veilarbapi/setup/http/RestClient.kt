package no.nav.poao.veilarbapi.setup.http

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import no.nav.common.rest.client.LogInterceptor
import java.util.concurrent.TimeUnit

fun baseEngine(): HttpClientEngine {
    return OkHttp.create {
        config {
            connectTimeout(10, TimeUnit.SECONDS)
            readTimeout(15, TimeUnit.SECONDS)
            followRedirects(false)
        }
        addInterceptor(LogInterceptor())
    }
}

fun baseClient(engine: HttpClientEngine): HttpClient {
    return HttpClient(engine) {
        expectSuccess = false
    }
}