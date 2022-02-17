package no.nav.poao.plugins

import io.ktor.application.*
import no.nav.poao.rest.arbeidsoppfolgingRoutes
import no.nav.poao.rest.internalRoutes

fun Application.configureRouting(useAuthentication: Boolean) {
    internalRoutes()
    arbeidsoppfolgingRoutes(useAuthentication)
}
