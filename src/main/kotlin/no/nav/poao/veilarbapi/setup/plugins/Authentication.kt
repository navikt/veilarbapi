package no.nav.poao.veilarbapi.setup.plugins

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import no.nav.poao.veilarbapi.setup.config.Configuration
import java.net.URL
import java.util.concurrent.TimeUnit

fun Application.configureAuthentication(useAuthentication: Boolean, azureAdConfig: Configuration.AzureAd ) {

    if (useAuthentication) {
        install(Authentication) {
            jwt("azuread") {
                verifier(
                    jwkProvider = JwkProviderBuilder(URL(azureAdConfig.openIdConfiguration.jwksUri))
                        .cached(10, 24, TimeUnit.HOURS)
                        .rateLimited(10, 1, TimeUnit.MINUTES)
                        .build(),
                    issuer = azureAdConfig.openIdConfiguration.issuer
                )
                validate { credentials ->
                    requireNotNull(credentials.payload.audience) {
                        "Auth: Missing audience in token"
                    }
                    require(credentials.payload.audience?.contains(azureAdConfig.clientId) == true) {
                        "Auth: Valid audience not found in claims"
                    }
                    JWTPrincipal(credentials.payload)
                }
            }
        }
    }
}