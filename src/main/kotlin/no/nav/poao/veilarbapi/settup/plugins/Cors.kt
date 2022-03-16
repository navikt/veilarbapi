package no.nav.poao.veilarbapi.settup.plugins

import io.ktor.application.*
import io.ktor.features.*

fun Application.configureCors() {
    install(CORS) {
        anyHost()
        allowCredentials = true
    }
}
