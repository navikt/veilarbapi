package no.nav.poao.util

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import no.nav.security.mock.oauth2.MockOAuth2Server

internal fun setupEnvironment(mockOAuth2Server: MockOAuth2Server) {
    System.setProperty("NAIS_APP_NAME", "local")
    System.setProperty("AZURE_APP_WELL_KNOWN_URL", "${mockOAuth2Server.wellKnownUrl("default")}")
    System.setProperty("AZURE_APP_CLIENT_ID", "client_id")
    System.setProperty("AZURE_APP_CLIENT_SECRET", "supersecret")
    System.setProperty("VEILARBAKTIVITETAPI_URL", "/veilarbaktivitet")
    System.setProperty("VEILARBDIALOGAPI_URL", "/veilarbdialog")
    System.setProperty("VEILARBOPPFOLGINGAPI_URL", "/veilarboppfolging")
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