package no.nav.poao.veilarbapi

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.poao.veilarbapi.config.Configuration
import no.nav.poao.veilarbapi.client.VeilarbaktivitetClient
import no.nav.poao.veilarbapi.plugins.configureAuthentication
import no.nav.poao.veilarbapi.plugins.configureMonitoring
import no.nav.poao.veilarbapi.plugins.configureRouting
import no.nav.poao.veilarbapi.plugins.configureSerialization

fun createHttpServer(
    applicationState: ApplicationState,
    port: Int = 8080,
    configuration: Configuration,
    veilarbaktivitetClient: VeilarbaktivitetClient
) : ApplicationEngine = embeddedServer(Netty, port, "0.0.0.0") {

    configureMonitoring()
    configureAuthentication(configuration)
    configureSerialization()
    configureRouting(configuration.useAuthentication, veilarbaktivitetClient = veilarbaktivitetClient)
    applicationState.initialized = true
}