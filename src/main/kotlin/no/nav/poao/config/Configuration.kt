package no.nav.poao.config

import com.auth0.jwk.JwkProvider
import com.natpryce.konfig.*
import no.nav.common.utils.Credentials
import no.nav.common.utils.NaisUtils.getCredentials
import no.nav.poao.JwtUtil

private const val notUsedLocally = ""
private val defaultProperties by lazy {
    ConfigurationMap(
        mapOf(
            "NAIS_CLUSTER_NAME" to notUsedLocally,
            "ISSO_JWKS_URL" to "https://isso-q.adeo.no/isso/oauth2/connect/jwk_uri",
            "ISSO_ISSUER_URL" to "https://isso-q.adeo.no:443/isso/oauth2",
            "VAULT_MOUNT_PATH" to notUsedLocally,
            "SECURITY_TOKEN_SERVICE_DISCOVERY_URL" to notUsedLocally,
            "VEILARBAPI_AAD_CLIENT_ID" to notUsedLocally,
            "AZUREAD_JWKS_URL" to notUsedLocally,
            "VEILARBAKTIVITETAPI_URL" to notUsedLocally,
            "VEILARBDIALOGAPI_URL" to notUsedLocally
        )
    )
}

data class Configuration(
    val veilarbaktivitetConfig: VeilarbaktivitetConfig = VeilarbaktivitetConfig(),
    val veilarbdialogConfig: VeilarbdialogConfig = VeilarbdialogConfig(),
    val clustername: String = config()[Key("NAIS_CLUSTER_NAME", stringType)],
    val stsDiscoveryUrl: String = config()[Key("SECURITY_TOKEN_SERVICE_DISCOVERY_URL", stringType)],
    val jwt: Jwt = Jwt(),
    val serviceUser: Credentials = getCredentials("service_user"),
    val httpServerWait: Boolean = true,
    val useAuthentication: Boolean = true
) {
    data class Jwt(
        val issoJwksUrl: JwkProvider = JwtUtil.makeJwkProvider(config()[Key("ISSO_JWKS_URL", stringType)]),
        val issoJwtIssuer: String = config()[Key("ISSO_ISSUER_URL", stringType)],
        val azureAdJwksUrl: JwkProvider = JwtUtil.makeJwkProvider(config()[Key("AZUREAD_JWKS_URL", stringType)]),
        val azureAdClientId: String = config()[Key("VEILARBAPI_AAD_CLIENT_ID", stringType)]
    )
    data class VeilarbaktivitetConfig(
        val url: String = config()[Key("VEILARBAKTIVITETAPI_URL", stringType)]
    )
    data class VeilarbdialogConfig(
        val url: String = config()[Key("VEILARBDIALOGAPI_URL", stringType)]
    )
}

private fun config() = ConfigurationProperties.systemProperties() overriding
        EnvironmentVariables overriding
        defaultProperties