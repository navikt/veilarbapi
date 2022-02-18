package no.nav.poao

import io.ktor.server.engine.*
import no.nav.common.utils.Credentials
import no.nav.poao.config.Configuration


fun mainTest(): ApplicationEngine {
    System.setProperty("NAIS_APP_NAME", "local")
    System.setProperty("AZUREAD_JWKS_URL", "https://login.microsoftonline.com/966ac572-f5b7-4bbe-aa88-c76419c0f851/discovery/v2.0/keys")

    val configuration = Configuration(
        clustername = "",
        stsDiscoveryUrl = "",
        serviceUser = Credentials("foo", "bar"),
    )

    val applicationState = ApplicationState()


    val applicationServer = createHttpServer(
        applicationState = applicationState,
        configuration = configuration,
    );

    Runtime.getRuntime().addShutdownHook(Thread {
        applicationState.initialized = false
        applicationServer.stop(0, 0)
    })

    applicationServer.start(wait = false)
    return applicationServer
}
