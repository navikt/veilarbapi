package no.nav.poao.veilarbapi.client


import com.github.michaelbull.result.get
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.java.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.common.utils.IdUtils
import no.nav.poao.veilarbapi.oauth.AzureAdClient
import no.nav.poao.veilarbapi.client.exceptions.IkkePaaLoggetException
import no.nav.poao.veilarbapi.client.exceptions.ManglerTilgangException
import no.nav.poao.veilarbapi.client.exceptions.ServerFeilException
import no.nav.veilarbaktivitet.JSON
import no.nav.veilarbaktivitet.model.Aktivitet
import no.nav.poao.veilarbapi.config.Cluster
import no.nav.poao.veilarbapi.config.Configuration

class VeilarbaktivitetClient constructor(val veilarbaktivitetConfig: Configuration.VeilarbaktivitetConfig, val poaoGcpProxyConfig: Configuration.PoaoGcpProxyConfig, val azureAdClient: AzureAdClient?, engine: HttpClientEngine = Java.create()) {

    val json = JSON()
    val client: HttpClient =
        HttpClient(engine) {
            expectSuccess = false
            defaultRequest {
                mandatoryHeaders()
            }
        }

    val veilarbaktivitetUrl = veilarbaktivitetConfig.url
    val veilarbaktivitetResource = veilarbaktivitetAuthenticationScope
    val poaoGcpProxyResource = poaoProxyAuthenticationScope

    fun hentAktivitet(aktivitetsId: Int, accessToken: String?): Aktivitet? {
        return runBlocking {
            val veilarbaktivitetOnBehalfOfAccessToken = accessToken?.let {
                azureAdClient?.getOnBehalfOfAccessTokenForResource(
                    scopes = listOf(veilarbaktivitetResource),
                    accessToken = it
                )
            }
            val poaoGcpProxyServiceUserAccessToken = accessToken?.let {
                azureAdClient?.getAccessTokenForResource(
                    scopes = listOf(poaoGcpProxyResource)
                )
            }
            client.use { httpClient ->
                val response =
                httpClient.get<HttpResponse>("$veilarbaktivitetUrl/internal/api/v1/aktivitet/$aktivitetsId") {
                    header(HttpHeaders.Authorization, "Bearer ${poaoGcpProxyServiceUserAccessToken?.get()}")
                    header("Downstream-Authorization", "Bearer ${veilarbaktivitetOnBehalfOfAccessToken?.get()}")
                }
                if (response.status == HttpStatusCode.OK) {
                        Aktivitet.fromJson(response.readText())
                } else {
                    throw callFailure(response)
                }
            }
        }
    }

    fun hentAktiviteter(aktorId: String, accessToken: String?): Array<Aktivitet> {
        return runBlocking {
            val veilarbaktivitetOnBehalfOfAccessToken = accessToken?.let {
                azureAdClient?.getOnBehalfOfAccessTokenForResource(
                    scopes = listOf(veilarbaktivitetResource),
                    accessToken = it
                )
            }
            val poaoGcpProxyServiceUserAccessToken = accessToken?.let {
                azureAdClient?.getAccessTokenForResource(
                    scopes = listOf(poaoGcpProxyResource)
                )
            }
            client.use { httpClient ->
                val response =
                    httpClient.get<HttpResponse>("$veilarbaktivitetUrl/internal/api/v1/aktivitet?aktorId=$aktorId") {
                        header(HttpHeaders.Authorization, "Bearer ${poaoGcpProxyServiceUserAccessToken?.get()}")
                        header("Downstream-Authorization", "Bearer ${veilarbaktivitetOnBehalfOfAccessToken?.get()}")
                    }
                if (response.status == HttpStatusCode.OK) {
                    JSON.deserialize<Array<Aktivitet>>(response.readText(), Aktivitet::class.java.arrayType())
                } else {
                    throw callFailure(response)
                }
            }
        }
    }


    fun HttpRequestBuilder.mandatoryHeaders() {
        header("Nav-Call-Id", IdUtils.generateId())
        header("Nav-Consumer-Id", "veilarbapi")
    }

    private suspend fun callFailure(response: HttpResponse): Exception {
        return when (response.status) {
            HttpStatusCode.Forbidden -> ManglerTilgangException(response, response.readText())
            HttpStatusCode.Unauthorized -> IkkePaaLoggetException(response, response.readText())
            HttpStatusCode.InternalServerError -> ServerFeilException(response, response.readText())
            else -> Exception("Ukjent statuskode ${response.status}")
        }
    }


    companion object {
        val poaoProxyAuthenticationScope by lazy { "api://${if (Cluster.current == Cluster.PROD_GCP) "prod-fss" else "dev-fss"}.pto.poao-gcp-proxy/.default" }
        val veilarbaktivitetAuthenticationScope by lazy { "api://${if (Cluster.current == Cluster.PROD_GCP) "prod-fss" else "dev-fss"}.pto.veilarbaktivitet/.default" }
    }

}