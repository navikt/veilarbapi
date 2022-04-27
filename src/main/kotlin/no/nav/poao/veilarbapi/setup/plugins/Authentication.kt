package no.nav.poao.veilarbapi.setup.plugins

import io.ktor.application.*
import io.ktor.auth.*
import no.nav.poao.veilarbapi.setup.oauth.azureAdtokenSupportConfig
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.security.token.support.ktor.tokenValidationSupport

fun Application.configureAuthentication(useAuthentication: Boolean, azureAdConfig: Configuration.AzureAd ) {

    if (useAuthentication) {
        val tokenSupportConfig = azureAdtokenSupportConfig(azureAdConfig)
        val tokenValidationConfig: Authentication.Configuration.() -> Unit = {
            tokenValidationSupport(config = tokenSupportConfig, name = "azuread")
        }
        install(Authentication, tokenValidationConfig)

    }

}