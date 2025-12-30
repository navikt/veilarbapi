package no.nav.poao.util

import io.ktor.server.config.MapApplicationConfig
import io.ktor.server.testing.ApplicationTestBuilder
import no.nav.security.mock.oauth2.MockOAuth2Server

class RealServerTestUtil {

    companion object {
        suspend fun withMockOAuth2ServerWithEnv(testBlock: suspend MockOAuth2Server.() -> Unit) {
            val server = MockOAuth2Server()
            server.start()
            System.setProperty("AZURE_APP_WELL_KNOWN_URL", "${server.wellKnownUrl("default")}")
            System.setProperty("AZURE_APP_CLIENT_ID", "clientid")
            System.setProperty("AZURE_APP_CLIENT_SECRET", "supersecret")
            server.testBlock()
            server.shutdown()
        }

        fun ApplicationTestBuilder.setupExternalEndpoints(block: () -> Unit) {
            externalServices {
            }
            block()
        }

        fun setDefaultTestSystemProperties() {
            System.setProperty("NAIS_APP_NAME", "local")
            System.setProperty("VEILARBAKTIVITETAPI_URL", "http://veilarbaktivitet/veilarbaktivitet")
            System.setProperty("VEILARBAKTIVITETAPI_SCOPE", "api://local.dab.veilarbaktivitet/.default")
            System.setProperty("VEILARBDIALOGAPI_URL", "http://veilarbdialog/veilarbdialog")
            System.setProperty("VEILARBOPPFOLGINGAPI_URL", "http://veilarboppfolging/veilarboppfolging")
            System.setProperty("VEILARBAKTIVITETAPI_SCOPE", "api://local.poao.veilarboppfolging/.default")
        }
    }
}

fun MockOAuth2Server.getMockOauth2ServerConfig(
    acceptedIssuer: String = "default",
    acceptedAudience: String = "default"): MapApplicationConfig {
    val server = this
    return MapApplicationConfig().apply {
        put("no.nav.security.jwt.issuers.size", "1")
        put("no.nav.security.jwt.issuers.0.issuer_name", acceptedIssuer)
        put("no.nav.security.jwt.issuers.0.discoveryurl", "${server.wellKnownUrl(acceptedIssuer)}")
        put("no.nav.security.jwt.issuers.0.accepted_audience", acceptedAudience)
    }
}