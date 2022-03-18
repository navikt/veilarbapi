package no.nav.poao.veilarbapi.setup.plugins

import io.ktor.application.*
import no.nav.poao.veilarbapi.oppfolging.Service
import no.nav.poao.veilarbapi.setup.rest.arbeidsoppfolgingRoutes
import no.nav.poao.veilarbapi.setup.rest.internalRoutes

fun Application.configureRouting(useAuthentication: Boolean, service: Service) {
    internalRoutes()
    arbeidsoppfolgingRoutes(useAuthentication, service = service)
}
