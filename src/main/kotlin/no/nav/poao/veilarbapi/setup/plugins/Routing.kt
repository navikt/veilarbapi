package no.nav.poao.veilarbapi.setup.plugins

import io.ktor.application.*
import no.nav.poao.veilarbapi.oppfolging.OppfolgingService
import no.nav.poao.veilarbapi.setup.rest.arbeidsoppfolgingRoutes
import no.nav.poao.veilarbapi.setup.rest.internalRoutes

fun Application.configureRouting(useAuthentication: Boolean, oppfolgingService: OppfolgingService) {
    internalRoutes()
    arbeidsoppfolgingRoutes(useAuthentication, oppfolgingService = oppfolgingService)
}
