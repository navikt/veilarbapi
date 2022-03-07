package no.nav.poao.veilarbapi.settup.oauth

import no.nav.poao.veilarbapi.settup.config.Configuration
import no.nav.security.token.support.ktor.IssuerConfig
import no.nav.security.token.support.ktor.TokenSupportConfig


fun azureAdtokenSupportConfig(azureAd: Configuration.AzureAd): TokenSupportConfig {
    val issuerConfig =  IssuerConfig(
            name = "azuread",
            discoveryUrl = azureAd.wellKnownConfigurationUrl,
            acceptedAudience = listOf(azureAd.clientId)
        )
    return TokenSupportConfig(issuerConfig)
}