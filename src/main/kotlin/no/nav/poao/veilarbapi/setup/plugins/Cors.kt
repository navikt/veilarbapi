package no.nav.poao.veilarbapi.setup.plugins

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.plugins.cors.routing.*

fun Application.configureCors() {
    install(CORS) {
        allowHost("navikt.github.io", schemes = listOf("https"))
        allowHost("localhost:8080")
        allowHeader(HttpHeaders.Authorization)
        allowHeader("Nav-Consumer-Id")
        allowHeader("Nav-Call-Id")
        allowMethod(HttpMethod.Options)
        allowCredentials = true
    }
}