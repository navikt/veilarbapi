package no.nav.poao.veilarbapi.setup.util

import com.github.michaelbull.result.get
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.poao.veilarbapi.setup.exceptions.IkkePaaLoggetException
import no.nav.poao.veilarbapi.setup.oauth.AzureAdClient

class TokenProviders(
    val azureAdClient: AzureAdClient,
    val configuration: Configuration
) {

    val veilarbaktivitetTokenProvider: suspend (String) -> String = { accessToken ->
        azureAdClient.getOnBehalfOfAccessTokenForResource(
            scopes = listOf(configuration.veilarbaktivitetConfig.authenticationScope),
            accessToken = accessToken
        ).let { it.get()?.accessToken ?: throw it.component2()!!.throwable }
    }

    val veilarbdialogTokenProvider: suspend (String) -> String = { accessToken ->
        azureAdClient.getOnBehalfOfAccessTokenForResource(
            scopes = listOf(configuration.veilarbdialogConfig.authenticationScope),
            accessToken = accessToken
        ).let { it.get()?.accessToken ?: throw it.component2()!!.throwable }
    }

    val veilarboppfolgingTokenProvider: suspend (String) -> String = { accessToken ->
        azureAdClient.getOnBehalfOfAccessTokenForResource(
            scopes = listOf(configuration.veilarboppfolgingConfig.authenticationScope),
            accessToken = accessToken
        ).let { it.get()?.accessToken ?: throw it.component2()!!.throwable }
    }

}