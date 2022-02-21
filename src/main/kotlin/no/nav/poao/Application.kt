package no.nav.poao

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.client.*
import io.ktor.client.engine.apache.*
import io.ktor.client.features.json.*
import io.ktor.http.auth.*
import no.nav.common.utils.SslUtils
import no.nav.poao.auth.ServiceToServiceTokenProvider
import no.nav.poao.client.VeilarbaktivitetClient
import no.nav.poao.config.Configuration
import org.apache.http.impl.conn.SystemDefaultRoutePlanner
import java.net.ProxySelector

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

    val veilarbaktivitetAccessTokenClient = ServiceToServiceTokenProvider(config = ServiceToServiceTokenProvider.Config(
        azureClientSecret = configuration.azureAd.clientSecret,
        azureClientId = configuration.azureAd.clientId,
        tokenEndpoint = configuration.azureAd.openIdConfiguration.tokenEndpoint,
        scope = VeilarbaktivitetClient.ptoProxyAuthenticationScope
    ))

    val applicationServer = createHttpServer(
        applicationState = applicationState,
        configuration = configuration
        )

    Runtime.getRuntime().addShutdownHook(Thread {
        applicationState.initialized = false
    })

    applicationServer.start(wait = configuration.httpServerWait)
}


private fun HttpAuthHeader.getBlob(): String? = when {
    this is HttpAuthHeader.Single && authScheme.toLowerCase() in listOf("bearer") -> blob
    else -> null
}

private fun ApplicationCall.getAccessToken(): String? = request.parseAuthorizationHeader()?.getBlob()
