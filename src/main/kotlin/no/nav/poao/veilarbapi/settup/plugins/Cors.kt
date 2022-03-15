package no.nav.poao.veilarbapi.settup.plugins

import io.ktor.application.*
import io.ktor.features.*

fun Application.configureCors() {
    install(CORS) {
        host(host = "dev.intern.nav.no", schemes = listOf("https"), subDomains = listOf("veilarbapi"))
        allowCredentials = true
    }
}
