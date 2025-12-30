package no.nav.poao.util

import com.github.tomakehurst.wiremock.WireMockServer
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import no.nav.security.mock.oauth2.MockOAuth2Server

internal fun setupEnvironment(wellKnownUrl: String) {
    System.setProperty("NAIS_APP_NAME", "local")
    System.setProperty("AZURE_APP_WELL_KNOWN_URL", "$wellKnownUrl")
    System.setProperty("AZURE_APP_CLIENT_ID", "client_id")
    System.setProperty("AZURE_APP_CLIENT_SECRET", "supersecret")
    System.setProperty("VEILARBAKTIVITETAPI_URL", "http://veilarbaktivitet/veilarbaktivitet")
    System.setProperty("VEILARBAKTIVITETAPI_SCOPE", "api://local.dab.veilarbaktivitet/.default")
    System.setProperty("VEILARBDIALOGAPI_URL", "http://veilarbdialog/veilarbdialog")
    System.setProperty("VEILARBOPPFOLGINGAPI_URL", "http://veilarboppfolging/veilarboppfolging")
    System.setProperty("VEILARBOPPFOLGINGAPI_SCOPE", "api://local.poao.veilarboppfolging/.default")
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