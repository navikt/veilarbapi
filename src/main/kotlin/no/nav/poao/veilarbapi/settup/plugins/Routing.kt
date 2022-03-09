package no.nav.poao.veilarbapi.settup.plugins

import io.ktor.application.*
import no.nav.poao.veilarbapi.oppfolging.Service
import no.nav.poao.veilarbapi.settup.rest.arbeidsoppfolgingRoutes
import no.nav.poao.veilarbapi.settup.rest.internalRoutes

fun Application.configureRouting(useAuthentication: Boolean, service: Service) {
    internalRoutes()
    arbeidsoppfolgingRoutes(useAuthentication, service = service)
}
