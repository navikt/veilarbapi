package no.nav.poao.auth

import com.github.michaelbull.result.expect
import com.nimbusds.jwt.SignedJWT
import com.nimbusds.oauth2.sdk.TokenRequest
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.poao.veilarbapi.setup.oauth.AzureAdClient
import no.nav.poao.veilarbapi.setup.http.defaultHttpClient
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.mock.oauth2.withMockOAuth2Server
import kotlin.test.*
import kotlin.test.assertEquals

class AzureAdTokenCallback(
    private val audience: List<String>,
    private val claims: Map<String, String> = emptyMap(),
    issuerId: String,
) : DefaultOAuth2TokenCallback(issuerId = issuerId, audience = audience) {
    override fun audience(tokenRequest: TokenRequest): List<String> = audience
    override fun addClaims(tokenRequest: TokenRequest): Map<String, Any> = claims
}

@InternalAPI
class AzureAdClientTest {
    private val oauth2Server = MockOAuth2Server()
    private val issuer = "some-issuer"
    private val clientId = "some-client-id"

    @Test
    fun `given valid credentials, getting access token for resource should succeed`() {
        withMockOAuth2Server {
            val resource = "some-resource"
            enqueueCallback(
                AzureAdTokenCallback(
                    audience = listOf(resource),
                    issuerId = issuer,
                    claims = mapOf("azp" to clientId)
                )
            )
            val config = getAzureConfig(this)
            val azureAdClient = AzureAdClient(config)
            val scopes = listOf("api://$resource/.default")
            runBlocking { azureAdClient.getAccessTokenForResource(scopes) }
                .expect { "should return a valid access token" }
                .let { accessTokenResponse ->
                    with(SignedJWT.parse(accessTokenResponse.accessToken)) {
                        assertEquals(listOf(resource), jwtClaimsSet.audience, "audience should only contain some-resource")
                        assertEquals(config.openIdConfiguration.issuer, jwtClaimsSet.issuer, "issuer should be some-issuer")
                        assertEquals(clientId, jwtClaimsSet.getStringClaim("azp"), "azp should be some-client-id")
                    }
                }
        }
    }

    @Test
    fun `given valid access token, exchanging access token for resource on behalf of caller should succeed`() {

        val resource = "some-resource"
        val scopes = listOf("api://$resource/.default")
        withMockOAuth2Server {
            enqueueCallback(
                AzureAdTokenCallback(
                    audience = listOf(resource),
                    issuerId = issuer
                )
            )
            val config = getAzureConfig(this)
            val azureAdClient = AzureAdClient(config)
            val originalAccessToken = oauth2Server.issueToken(
                issuerId = issuer,
                clientId = clientId,
                tokenCallback = AzureAdTokenCallback(
                    audience = listOf(clientId),
                    claims = mapOf("azp" to clientId),
                    issuerId = issuer
                )
            )
            runBlocking {
                azureAdClient.getOnBehalfOfAccessTokenForResource(scopes, originalAccessToken.serialize())
            }.expect { "should return a valid access token" }
                .let { accessTokenResponse ->
                    with(SignedJWT.parse(accessTokenResponse.accessToken)) {
                        assertEquals(listOf(resource), jwtClaimsSet.audience, "audience should only contain some-resource")
                        assertEquals(config.openIdConfiguration.issuer, jwtClaimsSet.issuer, "issuer should be some-issuer")
                        assertEquals(clientId, jwtClaimsSet.getStringClaim("azp"), "azp should be some-client-id")
                    }
                }
        }
    }

    private fun getAzureConfig(oauth2Server: MockOAuth2Server): Configuration.AzureAd {
        val wellKnownUrl = oauth2Server.wellKnownUrl(issuer).toString()
        return Configuration.AzureAd(
            clientId = clientId,
            clientSecret = "some-client-secret",
            wellKnownConfigurationUrl = wellKnownUrl,
            openIdConfiguration = runBlocking { defaultHttpClient.get(wellKnownUrl).body() }
        )
    }
}