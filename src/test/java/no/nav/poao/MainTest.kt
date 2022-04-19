package no.nav.poao

import io.ktor.server.engine.*
import no.nav.poao.veilarbapi.main
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.security.mock.oauth2.MockOAuth2Server


fun mainTest() {
    val mockOAuth2Server = MockOAuth2Server()
    mockOAuth2Server.start()

    System.setProperty("NAIS_APP_NAME", "local")
    System.setProperty("AZURE_APP_WELL_KNOWN_URL", "${mockOAuth2Server.wellKnownUrl("default")}")
    System.setProperty("AZURE_APP_CLIENT_ID", "clientid")
    System.setProperty("AZURE_APP_CLIENT_SECRET", "supersecret")
    System.setProperty("VEILARBAKTIVITETAPI_URL", "http://localhost:8080/veilarbaktivitet")
    System.setProperty("VEILARBDIALOGAPI_URL", "http://localhost:8080/veilarbdialog")
    System.setProperty("VEILARBOPPFOLGINGAPI_URL", "http://localhost:8080/veilarboppfolging")

    val configuration = Configuration(
    )

   main(configuration)

    Runtime.getRuntime().addShutdownHook(Thread {
        mockOAuth2Server.shutdown()
    })

}
