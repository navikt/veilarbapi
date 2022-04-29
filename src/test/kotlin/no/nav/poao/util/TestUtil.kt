package no.nav.poao.util

import com.github.tomakehurst.wiremock.WireMockServer
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import no.nav.security.mock.oauth2.MockOAuth2Server

internal fun setupEnvironment(mockOAuth2Server: MockOAuth2Server, wireMockServer: WireMockServer? = null) {
    val port = wireMockServer?.port() ?: 8080
    System.setProperty("NAIS_APP_NAME", "local")
    System.setProperty("AZURE_APP_WELL_KNOWN_URL", "${mockOAuth2Server.wellKnownUrl("default")}")
    System.setProperty("AZURE_APP_CLIENT_ID", "client_id")
    System.setProperty("AZURE_APP_CLIENT_SECRET", "supersecret")
    System.setProperty("VEILARBAKTIVITETAPI_URL", "http://localhost:${port}/veilarbaktivitet")
    System.setProperty("VEILARBDIALOGAPI_URL", "http://localhost:${port}/veilarbdialog")
    System.setProperty("VEILARBOPPFOLGINGAPI_URL", "http://localhost:${port}/veilarboppfolging")
}

internal fun createMockClient(block: MockRequestHandleScope.(HttpRequestData) -> HttpResponseData): HttpClient {
    return HttpClient(MockEngine) {
        expectSuccess = false

        engine {
            addHandler { request ->
                this.block(request)
            }
        }
    }
}