package no.nav.poao.rest

import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.google.gson.Gson
import com.nimbusds.jwt.SignedJWT
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.coroutines.runBlocking
import no.nav.common.types.identer.NavIdent
import no.nav.poao.veilarbapi.oppfolging.*
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.poao.veilarbapi.setup.oauth.AccessToken
import no.nav.poao.veilarbapi.setup.oauth.AzureAdClient
import no.nav.poao.veilarbapi.setup.oauth.ThrowableErrorMessage
import no.nav.poao.veilarbapi.setup.plugins.configureAuthentication
import no.nav.poao.veilarbapi.setup.plugins.configureSerialization
import no.nav.poao.veilarbapi.setup.rest.arbeidsoppfolgingRoutes
import no.nav.poao.veilarbapi.setup.util.TokenProviders
import no.nav.security.mock.oauth2.MockOAuth2Server
import no.nav.security.mock.oauth2.token.DefaultOAuth2TokenCallback
import no.nav.security.mock.oauth2.withMockOAuth2Server
import org.junit.AfterClass
import org.junit.Test
import org.mockito.kotlin.mock
import kotlin.test.assertEquals

class ArbeidsoppfolgingRoutesITest {


    companion object {
        private val server = MockOAuth2Server()

        private val configuration = Configuration(
            veilarbaktivitetConfig = Configuration.VeilarbaktivitetConfig(url = "http://localhost:8080/veilarbaktivitet"),
            veilarbdialogConfig = Configuration.VeilarbdialogConfig(url = "http://localhost:8080/veilarbdialog"),
            veilarboppfolgingConfig = Configuration.VeilarboppfolgingConfig(url = "http://localhost:8080/veilarboppfolging", authenticationScope = "api://test.ns.app/.default"),
            azureAd = Configuration.AzureAd(clientId = "client_id", wellKnownConfigurationUrl = server.wellKnownUrl("azuread").toString())
        )

        init {
            server.start()
        }
    }

    @Test
    fun `hent oppfolgingsinfo`() {
        val underOppfolgingDTO = UnderOppfolgingDTO(true)
        val underOppfolgingMock = Gson().toJson(underOppfolgingDTO)

        val veilederDTO = VeilederDTO(NavIdent("z123456"))
        val veilederMock = Gson().toJson(veilederDTO)

        val oppfolgingsenhetDTO = OppfolgingsenhetDTO("NAV Grünerløkka", "1234")
        val oppfolgingsenhetMock = Gson().toJson(oppfolgingsenhetDTO)

        val httpClient = HttpClient(MockEngine) {
            engine {
                addHandler { request ->
                    when (request.url.encodedPath) {
                        "/veilarboppfolging/api/v2/oppfolging" -> respondOk(underOppfolgingMock)
                        "/veilarboppfolging/api/v2/veileder" -> respondOk(veilederMock)
                        "/veilarboppfolging/api/person/oppfolgingsenhet" -> respondOk(oppfolgingsenhetMock)
                        else -> error("Unhandled ${request.url.encodedPath}")
                    }
                }
            }
        }

        val azureAdClient = AzureAdClient(configuration.azureAd)

        val tokenProviders = TokenProviders(azureAdClient, configuration)

        val veilarboppfolgingClient = VeilarboppfolgingClientImpl(
            baseUrl = configuration.veilarboppfolgingConfig.url,
            veilarboppfolgingTokenProvider = tokenProviders.veilarboppfolgingTokenProvider,
            proxyTokenProvider = tokenProviders.proxyTokenProvider,
            client = httpClient
        )

        val oppfolgingService = OppfolgingService(
            veilarbaktivitetClient = mock {},
            veilarbdialogClient = mock {},
            veilarboppfolgingClient = veilarboppfolgingClient
        )

//            val azureAdConfig = Configuration.AzureAd(
//                clientId = "client_id",
//                wellKnownConfigurationUrl = this.wellKnownUrl("azuread").toString()
//            )

        val initialToken: SignedJWT = server.issueToken(subject = "enduser")



        val tokenResponse: Result<AccessToken, ThrowableErrorMessage> = runBlocking {
            azureAdClient.getOnBehalfOfAccessTokenForResource(
                scopes = listOf(configuration.azureAd.clientId),
                accessToken = initialToken.serialize()
            )
        }
//                url = tokenEndpointUrl,
//                auth = Auth.PrivateKeyJwt(
//                    keyPair = KeyPairGenerator.getInstance("RSA").apply { initialize(2048) }.generateKeyPair(),
//                    clientId = "client1",
//                    tokenEndpoint = tokenEndpointUrl
//                ),
//                token = initialToken.serialize(),
//                scope = "targetScope"



        withTestApplication({
            configureAuthentication(true, configuration.azureAd)
            configureSerialization()
            arbeidsoppfolgingRoutes(oppfolgingService = oppfolgingService)
        }) {
            handleRequest(HttpMethod.Get, "/v1/oppfolging/info?aktorId=123") {
                this.addHeader("Authorization", "Bearer ${tokenResponse.get()?.accessToken}")
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }

    }
}