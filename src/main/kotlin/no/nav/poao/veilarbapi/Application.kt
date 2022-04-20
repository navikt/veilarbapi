package no.nav.poao.veilarbapi

import no.nav.common.utils.SslUtils
import no.nav.poao.veilarbapi.aktivitet.VeilarbaktivitetClientImpl
import no.nav.poao.veilarbapi.dialog.VeilarbdialogClientImpl
import no.nav.poao.veilarbapi.oppfolging.OppfolgingService
import no.nav.poao.veilarbapi.oppfolging.VeilarboppfolgingClientImpl
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.poao.veilarbapi.setup.http.createHttpServer
import no.nav.poao.veilarbapi.setup.oauth.AzureAdClient
import no.nav.poao.veilarbapi.setup.util.TokenProviders

data class ApplicationState(var running: Boolean = true, var initialized: Boolean = false)

fun main() {
    main(Configuration())
}

fun main(configuration: Configuration) {
    SslUtils.setupTruststore()
    val applicationState = ApplicationState()

    val azureAdClient = AzureAdClient(configuration.azureAd)

    val tokenProviders = TokenProviders(azureAdClient, configuration)

    val veilarbaktivitetClient = VeilarbaktivitetClientImpl(configuration.veilarbaktivitetConfig.url, tokenProviders.veilarbaktivitetTokenProvider, tokenProviders.proxyTokenProvider)
    val veilarbdialogClient = VeilarbdialogClientImpl(configuration.veilarbdialogConfig.url, tokenProviders.veilarbdialogTokenProvider, tokenProviders.proxyTokenProvider)
    val veilarboppfolgingClient = VeilarboppfolgingClientImpl(configuration.veilarboppfolgingConfig.url, tokenProviders.veilarboppfolgingTokenProvider, tokenProviders.proxyTokenProvider)

    val oppfolgingService = OppfolgingService(veilarbaktivitetClient = veilarbaktivitetClient, veilarbdialogClient = veilarbdialogClient, veilarboppfolgingClient =  veilarboppfolgingClient)

    val applicationServer = createHttpServer(
        applicationState = applicationState,
        configuration = configuration,
        oppfolgingService = oppfolgingService
    )

    Runtime.getRuntime().addShutdownHook(Thread {
        applicationState.initialized = false
    })

    applicationServer.start(wait = configuration.httpServerWait)
}
