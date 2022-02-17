package no.nav.poao

import no.nav.common.sts.NaisSystemUserTokenProvider
import no.nav.common.utils.SslUtils
import no.nav.poao.config.Configuration

data class ApplicationState(var running: Boolean = true, var initialized: Boolean = false)

fun main() {
    main(Configuration())
}

fun main(configuration: Configuration) {
    SslUtils.setupTruststore()
    val applicationState = ApplicationState()
    val systemUserTokenProvider = NaisSystemUserTokenProvider(
        configuration.stsDiscoveryUrl,
        configuration.serviceUser.username,
        configuration.serviceUser.password
    )

    val applicationServer = createHttpServer(
        applicationState = applicationState,
        configuration = configuration
        )

    Runtime.getRuntime().addShutdownHook(Thread {
        applicationState.initialized = false
    })

    applicationServer.start(wait = configuration.httpServerWait)
}
