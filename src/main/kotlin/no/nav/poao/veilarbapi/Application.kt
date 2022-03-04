package no.nav.poao.veilarbapi

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.http.auth.*
import no.nav.common.utils.SslUtils
import no.nav.poao.veilarbapi.client.VeilarbaktivitetClient
import no.nav.poao.veilarbapi.client.VeilarbdialogClient
import no.nav.poao.veilarbapi.config.Configuration
import no.nav.poao.veilarbapi.oauth.AzureAdClient
import no.nav.security.token.support.ktor.TokenValidationContextPrincipal
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import org.slf4j.LoggerFactory
import java.net.ProxySelector
private val logger = LoggerFactory.getLogger("Application")

data class ApplicationState(var running: Boolean = true, var initialized: Boolean = false)

internal val defaultHttpClient = HttpClient(Apache) {
    install(JsonFeature) {
        serializer = JacksonSerializer {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }
    engine {
        customizeClient { setRoutePlanner(SystemDefaultRoutePlanner(ProxySelector.getDefault())) }
    }
}

fun main() {
    main(Configuration())
}

fun main(configuration: Configuration) {
    SslUtils.setupTruststore()
    val applicationState = ApplicationState()

    val azureAdClient = AzureAdClient(configuration.azureAd)

    val veilarbaktivitetClient = VeilarbaktivitetClient(configuration.veilarbaktivitetConfig, configuration.poaoGcpProxyConfig, azureAdClient)
    val veilarbdialogClient = VeilarbdialogClient(configuration.veilarbdialogConfig, azureAdClient)

    val applicationServer = createHttpServer(
        applicationState = applicationState,
        configuration = configuration,
        veilarbaktivitetClient = veilarbaktivitetClient,
        veilarbdialogClient = veilarbdialogClient
        )

    Runtime.getRuntime().addShutdownHook(Thread {
        applicationState.initialized = false
    })

    applicationServer.start(wait = configuration.httpServerWait)
}


private fun HttpAuthHeader.getBlob(): String? = when {
    this is HttpAuthHeader.Single && authScheme.lowercase() in listOf("bearer") -> blob
    else -> null
}

fun ApplicationCall.getAccessToken(): String? = request.parseAuthorizationHeader()?.getBlob()
fun ApplicationCall.getTokenInfo(): Map<String, String>? = authentication
    .principal<TokenValidationContextPrincipal>()
    ?.let { principal ->
        logger.debug("found principal $principal")
        principal.context.firstValidToken.get().jwtTokenClaims.allClaims.entries
            .associate { claim -> claim.key to claim.value.toString() }
    }
