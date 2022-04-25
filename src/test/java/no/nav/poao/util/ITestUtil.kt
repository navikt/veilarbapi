package no.nav.poao.util

import com.github.tomakehurst.wiremock.WireMockServer
import no.nav.security.mock.oauth2.MockOAuth2Server

internal fun setupEnvironment(server: MockOAuth2Server) {
        System.setProperty("NAIS_APP_NAME", "local")
        System.setProperty("AZURE_APP_WELL_KNOWN_URL", "${server.wellKnownUrl("default")}")
        System.setProperty("AZURE_APP_CLIENT_ID", "client_id")
        System.setProperty("AZURE_APP_CLIENT_SECRET", "supersecret")
        System.setProperty("VEILARBAKTIVITETAPI_URL", "http://localhost:8080/veilarbaktivitet")
        System.setProperty("VEILARBDIALOGAPI_URL", "http://localhost:8080/veilarbdialog")
        System.setProperty("VEILARBOPPFOLGINGAPI_URL", "http://localhost:8080/veilarboppfolging")
    }

internal fun setupEnvironment(mockOAuth2Server: MockOAuth2Server, wireMockServer: WireMockServer) {
    val wiremockServerPort = wireMockServer.port()
    System.setProperty("NAIS_APP_NAME", "local")
    System.setProperty("AZURE_APP_WELL_KNOWN_URL", "${mockOAuth2Server.wellKnownUrl("default")}")
    System.setProperty("AZURE_APP_CLIENT_ID", "client_id")
    System.setProperty("AZURE_APP_CLIENT_SECRET", "supersecret")
    System.setProperty("VEILARBAKTIVITETAPI_URL", "http://localhost:${wiremockServerPort}/veilarbaktivitet")
    System.setProperty("VEILARBDIALOGAPI_URL", "http://localhost:${wiremockServerPort}/veilarbdialog")
    System.setProperty("VEILARBOPPFOLGINGAPI_URL", "http://localhost:${wiremockServerPort}/veilarboppfolging")
}

