package no.nav.poao.veilarbapi

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.github.michaelbull.result.get
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.features.json.*
import io.ktor.http.auth.*
import no.nav.common.utils.SslUtils
import no.nav.poao.veilarbapi.aktivitet.VeilarbaktivitetClientImpl
import no.nav.poao.veilarbapi.dialog.VeilarbdialogClientImpl
import no.nav.poao.veilarbapi.oppfolging.OppfolgingService
import no.nav.poao.veilarbapi.oppfolging.VeilarboppfolgingClientImpl
import no.nav.poao.veilarbapi.setup.config.Cluster
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.poao.veilarbapi.setup.oauth.AzureAdClient
import no.nav.security.token.support.ktor.TokenValidationContextPrincipal
import org.slf4j.LoggerFactory
import java.net.ProxySelector

private val logger = LoggerFactory.getLogger("Application")

data class ApplicationState(var running: Boolean = true, var initialized: Boolean = false)

internal val defaultHttpClient = HttpClient(OkHttp) {
    install(JsonFeature) {
        serializer = JacksonSerializer {
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            setSerializationInclusion(JsonInclude.Include.NON_NULL)
        }
    }
    engine {
        config {
            proxySelector(ProxySelector.getDefault())
        }
    }
}

fun main() {
    main(Configuration())
}

fun main(configuration: Configuration) {
    SslUtils.setupTruststore()
    val applicationState = ApplicationState()

    val azureAdClient = AzureAdClient(configuration.azureAd)

    val poaoProxyAuthenticationScope by lazy { "api://${if (Cluster.current == Cluster.PROD_GCP) "prod-fss" else "dev-fss"}.pto.poao-gcp-proxy/.default" }
    val proxyTokenProvider: suspend (String?) -> String? = { accessToken ->
        accessToken?.let {
            azureAdClient.getAccessTokenForResource(
                scopes = listOf(poaoProxyAuthenticationScope)
            ).get()?.accessToken
        }
    }

    val veilarbaktivitetTokenProvider: suspend (String?) -> String? = { accessToken ->
        accessToken?.let {
            azureAdClient.getOnBehalfOfAccessTokenForResource(
                scopes = listOf(VeilarbaktivitetClientImpl.veilarbaktivitetAuthenticationScope),
                accessToken = it
            ).get()?.accessToken
        }
    }

    val veilarbdialogTokenProvider: suspend (String?) -> String? = { accessToken ->
        accessToken?.let {
            azureAdClient.getOnBehalfOfAccessTokenForResource(
                scopes = listOf(VeilarbdialogClientImpl.veilarbdialogAuthenticationScope),
                accessToken = it
            ).get()?.accessToken
        }
    }

    val veilarboppfolgingTokenProvider: suspend (String?) -> String? = { accessToken ->
        accessToken?.let {
            azureAdClient.getOnBehalfOfAccessTokenForResource(
                scopes = listOf(VeilarboppfolgingClientImpl.veilarboppfolgingAuthenticationScope),
                accessToken = it
            ).get()?.accessToken
        }
    }

    val veilarbaktivitetClient = VeilarbaktivitetClientImpl(configuration.veilarbaktivitetConfig, veilarbaktivitetTokenProvider, proxyTokenProvider)
    val veilarbdialogClient = VeilarbdialogClientImpl(configuration.veilarbdialogConfig, veilarbdialogTokenProvider, proxyTokenProvider)
    val veilarboppfolgingClient = VeilarboppfolgingClientImpl(configuration.veilarboppfolgingConfig, veilarboppfolgingTokenProvider, proxyTokenProvider)
    val oppfolgingService = OppfolgingService(veilarbaktivitetClient = veilarbaktivitetClient, veilarbdialogClient = veilarbdialogClient, veilarboppfolgingClient =  veilarboppfolgingClient)

    val applicationServer = createHttpServer(
        applicationState = applicationState,
        configuration = configuration,
        oppfolgingService = oppfolgingService
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
