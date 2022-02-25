package no.nav.poao

import io.ktor.server.engine.*
import no.nav.poao.veilarbapi.ApplicationState
import no.nav.poao.veilarbapi.config.Configuration
import no.nav.poao.veilarbapi.createHttpServer


fun mainTest(): ApplicationEngine {
    System.setProperty("NAIS_APP_NAME", "local")
    System.setProperty("AZURE_APP_WELL_KNOWN_URL", "https://login.microsoftonline.com/966ac572-f5b7-4bbe-aa88-c76419c0f851/v2.0/.well-known/openid-configuration")

    val configuration = Configuration(
        clustername = "",
        useAuthentication = false
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

    return applicationServer
}
