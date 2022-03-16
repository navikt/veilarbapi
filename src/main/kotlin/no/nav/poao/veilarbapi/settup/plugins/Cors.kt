package no.nav.poao.veilarbapi.settup.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*

fun Application.configureCors() {
    install(CORS) {
        host("localhost:63342")
        host("navikt.github.io", schemes = listOf("https"))
        header(HttpHeaders.ContentType)
        header(HttpHeaders.Authorization)
        allowHeadersPrefixed("Nav-")
        method(HttpMethod.Options)
        allowCredentials = true
    }
}
