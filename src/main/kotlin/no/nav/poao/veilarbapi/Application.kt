package no.nav.poao.veilarbapi

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.common.utils.SslUtils
import no.nav.poao.veilarbapi.aktivitet.VeilarbaktivitetClientImpl
import no.nav.poao.veilarbapi.dialog.VeilarbdialogClientImpl
import no.nav.poao.veilarbapi.oppfolging.OppfolgingService
import no.nav.poao.veilarbapi.oppfolging.VeilarboppfolgingClientImpl
import no.nav.poao.veilarbapi.setup.config.Cluster
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.poao.veilarbapi.setup.oauth.AzureAdClient
import no.nav.poao.veilarbapi.setup.plugins.*
import no.nav.poao.veilarbapi.setup.util.TokenProviders


fun main() {
    SslUtils.setupTruststore()

    val httpServerWait = Cluster.current != Cluster.LOKAL

    embeddedServer(factory = Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait =  httpServerWait)
}

fun Application.module(configuration: Configuration = Configuration()) {
    val azureAdClient = AzureAdClient(configuration.azureAd)

    val tokenProviders = TokenProviders(azureAdClient, configuration)

    val veilarbaktivitetClient = VeilarbaktivitetClientImpl(configuration.veilarbaktivitetConfig.url, tokenProviders.veilarbaktivitetTokenProvider, tokenProviders.proxyTokenProvider, configuration.veilarbaktivitetConfig.httpClient)
    val veilarbdialogClient = VeilarbdialogClientImpl(configuration.veilarbdialogConfig.url, tokenProviders.veilarbdialogTokenProvider, tokenProviders.proxyTokenProvider, configuration.veilarbdialogConfig.httpClient)
    val veilarboppfolgingClient = VeilarboppfolgingClientImpl(configuration.veilarboppfolgingConfig.url, tokenProviders.veilarboppfolgingTokenProvider, tokenProviders.proxyTokenProvider, configuration.veilarboppfolgingConfig.httpClient)

    val oppfolgingService = OppfolgingService(veilarbaktivitetClient = veilarbaktivitetClient, veilarbdialogClient = veilarbdialogClient, veilarboppfolgingClient =  veilarboppfolgingClient)

    configureMonitoring()
    configureAuthentication(configuration.useAuthentication, configuration.azureAd)
    configureSerialization()
    configureRouting(configuration.useAuthentication, oppfolgingService = oppfolgingService)
    configureExceptionHandler()
}
