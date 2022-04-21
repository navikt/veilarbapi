package no.nav.poao.veilarbapi.setup.http

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.poao.veilarbapi.ApplicationState
import no.nav.poao.veilarbapi.oppfolging.OppfolgingService
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.poao.veilarbapi.setup.plugins.*

fun createHttpServer(
    applicationState: ApplicationState,
    port: Int = 8080,
    configuration: Configuration,
    oppfolgingService: OppfolgingService,
) : ApplicationEngine = embeddedServer(Netty, port, "0.0.0.0") {

    configureMonitoring()
    configureAuthentication(configuration.useAuthentication, configuration.azureAd)
    configureSerialization()
    configureRouting(configuration.useAuthentication, oppfolgingService = oppfolgingService)
    configureExceptionHandler()


    applicationState.initialized = true
}