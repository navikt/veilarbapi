package no.nav.poao.veilarbapi

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.poao.veilarbapi.oppfolging.Service
import no.nav.poao.veilarbapi.setup.plugins.configureAuthentication
import no.nav.poao.veilarbapi.setup.plugins.configureMonitoring
import no.nav.poao.veilarbapi.setup.plugins.configureRouting
import no.nav.poao.veilarbapi.setup.plugins.configureSerialization

fun createHttpServer(
    applicationState: ApplicationState,
    port: Int = 8080,
    configuration: Configuration,
    service: Service
) : ApplicationEngine = embeddedServer(Netty, port, "0.0.0.0") {

    configureMonitoring()
    configureAuthentication(configuration)
    configureSerialization()
    configureRouting(configuration.useAuthentication, service = service)
    applicationState.initialized = true
}