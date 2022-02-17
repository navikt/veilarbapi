package no.nav.poao

import com.auth0.jwk.JwkProvider
import com.auth0.jwk.JwkProviderBuilder
import com.auth0.jwt.impl.JWTParser
import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.DecodedJWT
import com.auth0.jwt.interfaces.Payload
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.auth.*
import org.slf4j.LoggerFactory
import java.net.URL
import java.util.*
import java.util.concurrent.TimeUnit

private val log = LoggerFactory.getLogger("veilarbapi.JwtConfig")

class JwtUtil {
    companion object {
        fun useJwtFromCookie(call: ApplicationCall, cookieName: String): HttpAuthHeader? {
            return try {
                val token = call.request.cookies[cookieName]
                parseAuthorizationHeader("Bearer $token")
            } catch (ex: Throwable) {
                log.error("Illegal HTTP auth header", ex)
                null
            }
        }

        fun makeJwkProvider(jwksUrl: String): JwkProvider =
            JwkProviderBuilder(URL(jwksUrl))
                .cached(10, 24, TimeUnit.HOURS)
                .rateLimited(10, 1, TimeUnit.MINUTES)
                .build()

        fun validateJWT(credentials: JWTCredential, clientId: String?): Principal? {
            return try {
                requireNotNull(credentials.payload.audience) { "Audience not present" }
                if (clientId != null && clientId.isNotEmpty()) {
                    require(credentials.payload.audience.contains(clientId))
                }
                JWTPrincipal(credentials.payload)
            } catch (e: Exception) {
                log.error("Failed to validateJWT token" + e.message, e)
                null
            }
        }

        private fun HttpAuthHeader.getBlob() = when {
            this is HttpAuthHeader.Single -> blob
            else -> null
        }

        private fun DecodedJWT.parsePayload(): Payload {
            val payloadString = String(Base64.getUrlDecoder().decode(payload))
            return JWTParser().parsePayload(payloadString)
        }
    }
}
