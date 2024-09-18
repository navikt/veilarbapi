package no.nav.poao.util

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import no.nav.poao.veilarbapi.main
import no.nav.security.mock.oauth2.MockOAuth2Server

class RealServerTestUtil {

    companion object {
        private var mockOauth2Server = MockOAuth2Server()
        private val wireMockServer: WireMockServer = WireMockServer(WireMockConfiguration.options().dynamicPort())

        fun setup() {
            wireMockServer.start()
            mockOauth2Server.start()

            System.setProperty("NAIS_APP_NAME", "local")
            System.setProperty("AZURE_APP_WELL_KNOWN_URL", "${mockOauth2Server.wellKnownUrl("default")}")
            System.setProperty("AZURE_APP_CLIENT_ID", "clientid")
            System.setProperty("AZURE_APP_CLIENT_SECRET", "supersecret")
            System.setProperty("VEILARBAKTIVITETAPI_URL", "http://localhost:${wireMockServer.port()}/veilarbaktivitet")
            System.setProperty("VEILARBAKTIVITETAPI_SCOPE", "api://local.dab.veilarbaktivitet/.default")
            System.setProperty("VEILARBDIALOGAPI_URL", "http://localhost:${wireMockServer.port()}/veilarbdialog")
            System.setProperty("VEILARBOPPFOLGINGAPI_URL", "http://localhost:${wireMockServer.port()}/veilarboppfolging")
            System.setProperty("VEILARBAKTIVITETAPI_SCOPE", "api://local.poao.veilarboppfolging/.default")

            main()

            Runtime.getRuntime().addShutdownHook(Thread {
                mockOauth2Server.shutdown()
            })
        }
    }
}

