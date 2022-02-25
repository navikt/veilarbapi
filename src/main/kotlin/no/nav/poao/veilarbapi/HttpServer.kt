package no.nav.poao.veilarbapi

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.common.log.LogFilter
import no.nav.common.utils.EnvironmentUtils
import no.nav.poao.veilarbapi.config.Configuration
import no.nav.poao.veilarbapi.plugins.*
import no.nav.poao.veilarbapi.ApplicationState
import no.nav.poao.veilarbapi.plugins.configureAuthentication
import no.nav.poao.veilarbapi.plugins.configureMonitoring
import no.nav.poao.veilarbapi.plugins.configureRouting
import no.nav.poao.veilarbapi.plugins.configureSerialization

fun createHttpServer(
    applicationState: ApplicationState,
    port: Int = 8080,
    configuration: Configuration
) : ApplicationEngine = embeddedServer(Netty, port, "0.0.0.0") {


    configureMonitoring()
    configureAuthentication(configuration)
    configureSerialization()

    install(CallLogging) {
        LogFilter(EnvironmentUtils.requireApplicationName(), EnvironmentUtils.isDevelopment().orElse(false))
    }

    configureRouting(configuration.useAuthentication)
    applicationState.initialized = true
}