package no.nav.poao.veilarbapi.setup.util

import com.github.michaelbull.result.get
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.poao.veilarbapi.setup.oauth.AzureAdClient

class TokenProviders(
    val azureAdClient: AzureAdClient,
    val configuration: Configuration
) {

    val proxyTokenProvider: suspend (String?) -> String? = { accessToken ->
        accessToken?.let {
            azureAdClient.getAccessTokenForResource(
                scopes = listOf(configuration.poaoGcpProxyConfig.authenticationScope)
            ).get()?.accessToken
        }
    }

    val veilarbaktivitetTokenProvider: suspend (String?) -> String? = { accessToken ->
        accessToken?.let {
            azureAdClient.getOnBehalfOfAccessTokenForResource(
                scopes = listOf(configuration.veilarbaktivitetConfig.authenticationScope),
                accessToken = it
            ).get()?.accessToken
        }
    }

    val veilarbdialogTokenProvider: suspend (String?) -> String? = { accessToken ->
        accessToken?.let {
            azureAdClient.getOnBehalfOfAccessTokenForResource(
                scopes = listOf(configuration.veilarbdialogConfig.authenticationScope),
                accessToken = it
            ).get()?.accessToken
        }
    }

    val veilarboppfolgingTokenProvider: suspend (String?) -> String? = { accessToken ->
        accessToken?.let {
            azureAdClient.getOnBehalfOfAccessTokenForResource(
                scopes = listOf(configuration.veilarboppfolgingConfig.authenticationScope),
                accessToken = it
            ).get()?.accessToken
        }
    }

}