package no.nav.poao.config

import com.fasterxml.jackson.annotation.JsonProperty
import com.natpryce.konfig.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import no.nav.poao.defaultHttpClient

private const val notUsedLocally = ""
private val defaultProperties by lazy {
    ConfigurationMap(
        mapOf(
            "NAIS_CLUSTER_NAME" to notUsedLocally,
            "VEILARBAKTIVITETAPI_URL" to notUsedLocally,
            "VEILARBAKTIVITETAPI_CLIENTID" to notUsedLocally,
            "VEILARBDIALOGAPI_URL" to notUsedLocally,
            "VEILARBDIALOGAPI_CLIENTID" to notUsedLocally,
            "AZURE_APP_CLIENT_SECRET" to notUsedLocally,
            "AZURE_APP_CLIENT_ID" to notUsedLocally,
            "AZURE_APP_WELL_KNOWN_URL" to "https://login.microsoftonline.com/966ac572-f5b7-4bbe-aa88-c76419c0f851/v2.0/.well-known/openid-configuration"
        )
    )
}

data class Configuration(
    val veilarbaktivitetConfig: VeilarbaktivitetConfig = VeilarbaktivitetConfig(),
    val veilarbdialogConfig: VeilarbdialogConfig = VeilarbdialogConfig(),
    val clustername: String = config()[Key("NAIS_CLUSTER_NAME", stringType)],
    val azureAd: AzureAd = AzureAd(),
    val httpServerWait: Boolean = true,
    val useAuthentication: Boolean = true
) {
    data class AzureAd(
        val clientId: String = config()[Key("AZURE_APP_CLIENT_ID", stringType)],
        val clientSecret: String = config()[Key("AZURE_APP_CLIENT_SECRET", stringType)],
        val wellKnownConfigurationUrl: String = config()[Key("AZURE_APP_WELL_KNOWN_URL", stringType)],
        val openIdConfiguration: AzureAdOpenIdConfiguration = runBlocking {
            defaultHttpClient.get(wellKnownConfigurationUrl)
        }
    )

    data class VeilarbaktivitetConfig(
        val url: String = config()[Key("VEILARBAKTIVITETAPI_URL", stringType)],
        val clientId: String = config()[Key("VEILARBAKTIVITETAPI_CLIENTID", stringType)]
    )
    data class VeilarbdialogConfig(
        val url: String = config()[Key("VEILARBDIALOGAPI_URL", stringType)],
        val clientId: String = config()[Key("VEILARBDIALOGAPI_CLIENTID", stringType)]
    )
}

data class AzureAdOpenIdConfiguration(
    @JsonProperty("jwks_uri")
    val jwksUri: String,
    @JsonProperty("issuer")
    val issuer: String,
    @JsonProperty("token_endpoint")
    val tokenEndpoint: String,
    @JsonProperty("authorization_endpoint")
    val authorizationEndpoint: String
)

private fun config() = ConfigurationProperties.systemProperties() overriding
        EnvironmentVariables overriding
        defaultProperties