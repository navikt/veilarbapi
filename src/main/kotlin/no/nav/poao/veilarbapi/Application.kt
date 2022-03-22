package no.nav.poao.veilarbapi

import com.github.michaelbull.result.get
import no.nav.common.utils.SslUtils
import no.nav.poao.veilarbapi.aktivitet.VeilarbaktivitetClientImpl
import no.nav.poao.veilarbapi.dialog.VeilarbdialogClientImpl
import no.nav.poao.veilarbapi.oppfolging.OppfolgingService
import no.nav.poao.veilarbapi.oppfolging.VeilarboppfolgingClientImpl
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.poao.veilarbapi.setup.http.createHttpServer
import no.nav.poao.veilarbapi.setup.oauth.AzureAdClient

data class ApplicationState(var running: Boolean = true, var initialized: Boolean = false)

fun main() {
    main(Configuration())
}

fun main(configuration: Configuration) {
    SslUtils.setupTruststore()
    val applicationState = ApplicationState()

    val azureAdClient = AzureAdClient(configuration.azureAd)

    val proxyTokenProvider: suspend (String?) -> String? = { accessToken ->
        accessToken?.let {
            azureAdClient.getAccessTokenForResource(
                scopes = listOf(configuration.poaoGcpProxyConfig.authenticationScope)
            ).get()?.accessToken
        }
    }

    val veilarbaktivitetTokenProvider: suspend (String?) -> String? = { accessToken ->
        accessToken?.let {
            azureAdClient.getOnBehalfOfAccessTokenForResource(
                scopes = listOf(configuration.veilarbaktivitetConfig.authenticationScope),
                accessToken = it
            ).get()?.accessToken
        }
    }

    val veilarbdialogTokenProvider: suspend (String?) -> String? = { accessToken ->
        accessToken?.let {
            azureAdClient.getOnBehalfOfAccessTokenForResource(
                scopes = listOf(configuration.veilarbdialogConfig.authenticationScope),
                accessToken = it
            ).get()?.accessToken
        }
    }

    val veilarboppfolgingTokenProvider: suspend (String?) -> String? = { accessToken ->
        accessToken?.let {
            azureAdClient.getOnBehalfOfAccessTokenForResource(
                scopes = listOf(configuration.veilarboppfolgingConfig.authenticationScope),
                accessToken = it
            ).get()?.accessToken
        }
    }

    val veilarbaktivitetClient = VeilarbaktivitetClientImpl(configuration.veilarbaktivitetConfig.url, veilarbaktivitetTokenProvider, proxyTokenProvider)
    val veilarbdialogClient = VeilarbdialogClientImpl(configuration.veilarbdialogConfig.url, veilarbdialogTokenProvider, proxyTokenProvider)
    val veilarboppfolgingClient = VeilarboppfolgingClientImpl(configuration.veilarboppfolgingConfig.url, veilarboppfolgingTokenProvider, proxyTokenProvider)

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
