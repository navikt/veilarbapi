package no.nav.poao.veilarbapi

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.poao.veilarbapi.oppfolging.Service
import no.nav.poao.veilarbapi.settup.config.Cluster
import no.nav.poao.veilarbapi.settup.config.Configuration
import no.nav.poao.veilarbapi.settup.plugins.*

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

    if (Cluster.current == Cluster.DEV_GCP) {
        configureCors()
    }

    applicationState.initialized = true
}