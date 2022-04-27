package no.nav.poao.veilarbapi.setup.http

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.json.*
import io.ktor.http.*
import no.nav.common.rest.client.LogInterceptor
import java.net.ProxySelector
import java.util.concurrent.TimeUnit

val HttpHeaders.DownstreamAuthorization: String
    get() = "Downstream-Authorization"

internal val defaultHttpClient = HttpClient(OkHttp) {
    install(JsonFeature) {
        serializer = JacksonSerializer {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }
    engine {
        config {
            proxySelector(ProxySelector.getDefault())
        }
    }
}

fun baseClient(): HttpClient {
    return baseClient(baseEngine())
}

fun baseClient(engine: HttpClientEngine): HttpClient {
    return HttpClient(engine) {
        expectSuccess = false
    }
}

private fun baseEngine(): HttpClientEngine {
    return OkHttp.create {
        config {
            connectTimeout(10, TimeUnit.SECONDS)
            readTimeout(15, TimeUnit.SECONDS)
            followRedirects(false)
        }
        addInterceptor(LogInterceptor())
    }
}
