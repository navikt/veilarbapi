package no.nav.poao.veilarbapi.setup.util

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.auth.*
import no.nav.security.token.support.ktor.TokenValidationContextPrincipal
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("Application")

fun ApplicationCall.getAccessToken(): String? = request.parseAuthorizationHeader()?.getBlob()

fun ApplicationCall.getTokenInfo(): Map<String, String>? = authentication
    .principal<TokenValidationContextPrincipal>()
    ?.let { principal ->
        logger.debug("found principal $principal")
        principal.context.firstValidToken.get().jwtTokenClaims.allClaims.entries
            .associate { claim -> claim.key to claim.value.toString() }
    }

private fun HttpAuthHeader.getBlob(): String? = when {
    this is HttpAuthHeader.Single && authScheme.lowercase() in listOf("bearer") -> blob
    else -> null
}