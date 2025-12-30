package no.nav.poao.veilarbapi.setup.config

import com.natpryce.konfig.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import no.nav.poao.veilarbapi.setup.http.baseClient
import no.nav.poao.veilarbapi.setup.http.defaultHttpClient

private const val notUsedLocally = ""
private val defaultProperties by lazy {
    ConfigurationMap(
        mapOf(
            "NAIS_CLUSTER_NAME" to notUsedLocally,
            "VEILARBAKTIVITETAPI_URL" to notUsedLocally,
            "VEILARBAKTIVITETAPI_SCOPE" to notUsedLocally,
            "VEILARBDIALOGAPI_URL" to notUsedLocally,
            "VEILARBOPPFOLGINGAPI_URL" to notUsedLocally,
            "VEILARBOPPFOLGINGAPI_SCOPE" to notUsedLocally,
            "AZURE_APP_CLIENT_SECRET" to notUsedLocally,
            "AZURE_APP_CLIENT_ID" to notUsedLocally,
            "AZURE_APP_WELL_KNOWN_URL" to notUsedLocally
        )
    )
}

data class Configuration(
    val veilarbaktivitetConfig: VeilarbaktivitetConfig = VeilarbaktivitetConfig(),
    val veilarbdialogConfig: VeilarbdialogConfig = VeilarbdialogConfig(),
    val veilarboppfolgingConfig: VeilarboppfolgingConfig = VeilarboppfolgingConfig(),
    val clustername: String = config()[Key("NAIS_CLUSTER_NAME", stringType)],
    val azureAd: AzureAd = AzureAd(),
    val useAuthentication: Boolean = true
) {
    data class AzureAd(
        val clientId: String = config()[Key("AZURE_APP_CLIENT_ID", stringType)],
        val clientSecret: String = config()[Key("AZURE_APP_CLIENT_SECRET", stringType)],
        val wellKnownConfigurationUrl: String = config()[Key("AZURE_APP_WELL_KNOWN_URL", stringType)],
        val openIdConfiguration: AzureAdOpenIdConfiguration = runBlocking {
            defaultHttpClient.get(wellKnownConfigurationUrl).body()
        }
    )

    data class VeilarbaktivitetConfig(
        val url: String = config()[Key("VEILARBAKTIVITETAPI_URL", stringType)],
        val authenticationScope: String = config()[Key("VEILARBAKTIVITETAPI_SCOPE", stringType)],
        val httpClient: HttpClient = baseClient()
    )
    data class VeilarbdialogConfig(
        val url: String = config()[Key("VEILARBDIALOGAPI_URL", stringType)],
        val authenticationScope: String = "api://${Cluster.current.toGcp()}.dab.veilarbdialog/.default",
        val httpClient: HttpClient = baseClient()
    )
    data class VeilarboppfolgingConfig(
        val url: String = config()[Key("VEILARBOPPFOLGINGAPI_URL", stringType)],
        val authenticationScope: String = config()[Key("VEILARBOPPFOLGINGAPI_SCOPE", stringType)],
        val httpClient: HttpClient = baseClient()
    )
}

@Serializable
data class AzureAdOpenIdConfiguration(
    @SerialName("jwks_uri")
    val jwksUri: String,
    @SerialName("issuer")
    val issuer: String,
    @SerialName("token_endpoint")
    val tokenEndpoint: String,
    @SerialName("authorization_endpoint")
    val authorizationEndpoint: String
)

private fun config() = ConfigurationProperties.systemProperties() overriding
        EnvironmentVariables overriding
        defaultProperties