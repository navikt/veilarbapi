package no.nav.poao.plugins

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import no.nav.poao.JwtUtil
import no.nav.poao.config.AuthCookies
import no.nav.poao.config.Configuration

fun Application.configureAuthentication(configuration: Configuration) {
    if (configuration.useAuthentication)
    install(Authentication) {
        jwt("AzureAD") {
            skipWhen { applicationCall -> applicationCall.request.cookies[AuthCookies.AZURE_AD.cookieName] == null }
            realm = "veilarbapi"
            authHeader { applicationCall ->
                JwtUtil.useJwtFromCookie(
                    applicationCall,
                    AuthCookies.AZURE_AD.cookieName
                )
            }
            verifier(configuration.jwt.azureAdJwksUrl)
            validate { JwtUtil.validateJWT(it, configuration.jwt.azureAdClientId) }
        }
    }

}