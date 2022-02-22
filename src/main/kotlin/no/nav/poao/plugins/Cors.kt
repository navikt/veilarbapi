package no.nav.poao.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*

fun Application.configureCors() {
    install(CORS) {
        host("*.dev.adeo.no", schemes = listOf("http","https"))
        method(HttpMethod.Get)

        allowCredentials = true
    }
}