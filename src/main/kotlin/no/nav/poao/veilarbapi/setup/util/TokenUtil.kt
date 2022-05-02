package no.nav.poao.veilarbapi.setup.util

import io.ktor.server.application.*
import io.ktor.http.auth.*
import io.ktor.server.auth.*
import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.ktor.TokenValidationContextPrincipal
import org.slf4j.LoggerFactory

fun ApplicationCall.getAccessToken(): String? = request.parseAuthorizationHeader()?.getBlob()

private fun HttpAuthHeader.getBlob(): String? = when {
    this is HttpAuthHeader.Single && authScheme.lowercase() in listOf("bearer") -> blob
    else -> null
}