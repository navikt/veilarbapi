package no.nav.poao.veilarbapi.settup.plugins

import io.ktor.application.*
import io.ktor.features.*

fun Application.configureCors() {
    install(CORS) {
        host("localhost:63342")
        allowCredentials = true
    }
}
