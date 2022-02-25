package no.nav.poao.veilarbapi.plugins

import io.ktor.application.*
import no.nav.poao.veilarbapi.rest.arbeidsoppfolgingRoutes
import no.nav.poao.veilarbapi.rest.internalRoutes

fun Application.configureRouting(useAuthentication: Boolean) {
    internalRoutes()
    arbeidsoppfolgingRoutes(useAuthentication)
}
