package no.nav.poao.rest

import com.auth0.jwt.JWT
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.get
import com.google.gson.Gson
import com.nimbusds.jwt.SignedJWT
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
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
import org.assertj.core.api.Assertions
import org.junit.Test
import org.mockito.kotlin.mock
import kotlin.test.assertEquals

class ArbeidsoppfolgingRoutesITest {


    companion object {
        private val server = MockOAuth2Server()

        init {
            server.start()
        }
    }

    private val configuration = Configuration(
        veilarbaktivitetConfig = Configuration.VeilarbaktivitetConfig(url = "http://localhost:8080/veilarbaktivitet"),
        veilarbdialogConfig = Configuration.VeilarbdialogConfig(url = "http://localhost:8080/veilarbdialog"),
        veilarboppfolgingConfig = Configuration.VeilarboppfolgingConfig(url = "http://localhost:8080/veilarboppfolging"),
        azureAd = Configuration.AzureAd(clientId = "client_id", clientSecret="supersecret", wellKnownConfigurationUrl = server.wellKnownUrl("azuread").toString())
    )


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
                        "/veilarboppfolging/api/v2/oppfolging" -> {
                            checkTokenContent(request)
                            respondOk(underOppfolgingMock)
                        }
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

        val initialToken: SignedJWT = server.issueToken(subject = "enduser")


        val tokenResponse: Result<AccessToken, ThrowableErrorMessage> = runBlocking {
            azureAdClient.getOnBehalfOfAccessTokenForResource(
                scopes = listOf(configuration.azureAd.clientId),
                accessToken = initialToken.serialize()
            )
        }

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

    fun checkTokenContent(request: HttpRequestData) {
        val downstreamAuthString = request.headers.get("Downstream-Authorization")?.substringAfter("Bearer ")
        val downstreamAuthJwt = JWT.decode(downstreamAuthString)
        Assertions.assertThat(downstreamAuthJwt.subject).isEqualTo("enduser")
        Assertions.assertThat(downstreamAuthJwt.audience).containsExactly("api://local.pto.veilarboppfolging/.default")

        val authString = request.headers.get("Authorization")?.substringAfter("Bearer ")
        val authJwt = JWT.decode(authString)
        Assertions.assertThat(authJwt.subject).isEqualTo("client_id")
        Assertions.assertThat(authJwt.audience).containsExactly("api://local.pto.poao-gcp-proxy/.default")
    }
}