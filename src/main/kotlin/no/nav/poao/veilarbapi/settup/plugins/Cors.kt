package no.nav.poao.veilarbapi.settup.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*

fun Application.configureCors() {
    install(CORS) {
        host("localhost:63342")
        host("https://navikt.github.io/veilarbapi")
        method(HttpMethod.Options)
        allowCredentials = true
    }
}
