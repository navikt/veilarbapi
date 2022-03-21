package no.nav.poao

import io.ktor.server.engine.*
import no.nav.poao.veilarbapi.ApplicationState
import no.nav.poao.veilarbapi.aktivitet.VeilarbaktivitetClientImpl
import no.nav.poao.veilarbapi.createHttpServer
import no.nav.poao.veilarbapi.dialog.VeilarbdialogClientImpl
import no.nav.poao.veilarbapi.oppfolging.OppfolgingService
import no.nav.poao.veilarbapi.oppfolging.VeilarboppfolgingClientImpl
import no.nav.poao.veilarbapi.setup.config.Configuration


fun mainTest(): ApplicationEngine {
    System.setProperty("NAIS_APP_NAME", "local")
    System.setProperty("AZURE_APP_WELL_KNOWN_URL", "https://login.microsoftonline.com/966ac572-f5b7-4bbe-aa88-c76419c0f851/v2.0/.well-known/openid-configuration")

    val configuration = Configuration(
        clustername = "",
        useAuthentication = false
    )

    val applicationState = ApplicationState()

    val veilarbaktivitetClient = VeilarbaktivitetClientImpl(configuration.veilarbaktivitetConfig, { "VEILARBAKTIVITET_TOKEN" }, { "PROXY_TOKEN" })
    val veilarbdialogClient = VeilarbdialogClientImpl(configuration.veilarbdialogConfig, { "VEILARBDIALOG_TOKEN" }, { "PROXY_TOKEN" })
    val veilarboppfolgingClient = VeilarboppfolgingClientImpl(configuration.veilarboppfolgingConfig, { "VEILARBOPPFOLGING_TOKEN" }, { "PROXY_TOKEN" })

    val oppfolgingService = OppfolgingService(veilarbaktivitetClient, veilarbdialogClient, veilarboppfolgingClient)
    val applicationServer = createHttpServer(
        applicationState = applicationState,
        configuration = configuration,
        oppfolgingService = oppfolgingService
    )

    Runtime.getRuntime().addShutdownHook(Thread {
        applicationState.initialized = false
        applicationServer.stop(0, 0)
    })

    return applicationServer
}
