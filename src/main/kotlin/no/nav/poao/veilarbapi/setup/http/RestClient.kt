package no.nav.poao.veilarbapi.setup.http

import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.*

import io.ktor.http.*
import io.ktor.serialization.jackson.*
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

internal val defaultHttpClient = HttpClient {
    install(ContentNegotiation) {
        json(Json {
            ignoreUnknownKeys = true
            explicitNulls = true
        })
    }

    install(HttpTimeout) {
        connectTimeoutMillis = 5_000
        this@HttpClient.followRedirects = true
    }
}

fun baseClient(): HttpClient {
    return baseClient(null)
}

fun baseClient(engine: HttpClientEngine? = null): HttpClient {
    if (engine == null) return HttpClient(CIO) {
        expectSuccess = false
    }
    return HttpClient(engine) {
        expectSuccess = false
    }
}
