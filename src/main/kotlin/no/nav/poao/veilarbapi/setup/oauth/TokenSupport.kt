package no.nav.poao.veilarbapi.setup.oauth

import no.nav.poao.veilarbapi.setup.config.Cluster
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.security.token.support.ktor.IssuerConfig
import no.nav.security.token.support.ktor.TokenSupportConfig


fun azureAdtokenSupportConfig(azureAd: Configuration.AzureAd): TokenSupportConfig {
    val issuerConfig =  IssuerConfig(
            name = "azuread",
            discoveryUrl = azureAd.wellKnownConfigurationUrl,
            acceptedAudience = listOf(azureAd.clientId, "api://${Cluster.current.asString()}.pto.veilarbapi/.default")
        )
    return TokenSupportConfig(issuerConfig)
}