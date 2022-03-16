package no.nav.poao.veilarbapi.settup.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*

fun Application.configureCors() {
    install(CORS) {
        host("navikt.github.io", schemes = listOf("https"))
        header(HttpHeaders.Authorization)
        header("Nav-Consumer-Id")
        header("Nav-Call-Id")
        method(HttpMethod.Options)
        allowCredentials = true
    }
}
