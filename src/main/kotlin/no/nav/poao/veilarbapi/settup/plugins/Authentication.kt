package no.nav.poao.veilarbapi.settup.plugins

import io.ktor.application.*
import io.ktor.auth.*
import no.nav.poao.veilarbapi.settup.oauth.azureAdtokenSupportConfig
import no.nav.poao.veilarbapi.settup.config.Configuration
import no.nav.security.token.support.ktor.tokenValidationSupport

fun Application.configureAuthentication(configuration: Configuration) {


    if (configuration.useAuthentication) {
        val tokenSupportConfig = azureAdtokenSupportConfig(configuration.azureAd)
        val tokenValidationConfig: Authentication.Configuration.() -> Unit = {
            tokenValidationSupport(config = tokenSupportConfig, name = "azuread")
        }
        install(Authentication, tokenValidationConfig)

    }

}