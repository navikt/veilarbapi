package no.nav.poao.veilarbapi.client


import com.github.michaelbull.result.get
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.nav.common.utils.IdUtils
import no.nav.poao.veilarbapi.oauth.AzureAdClient
import no.nav.poao.veilarbapi.client.exceptions.IkkePaaLoggetException
import no.nav.poao.veilarbapi.client.exceptions.ManglerTilgangException
import no.nav.poao.veilarbapi.client.exceptions.ServerFeilException
import no.nav.veilarbaktivitet.JSON
import no.nav.veilarbaktivitet.model.Aktivitet
import no.nav.poao.veilarbapi.config.Cluster
import no.nav.poao.veilarbapi.config.Configuration

class VeilarbaktivitetClient constructor(val veilarbaktivitetConfig: Configuration.VeilarbaktivitetConfig, val poaoGcpProxyConfig: Configuration.PoaoGcpProxyConfig, val azureAdClient: AzureAdClient?, val engine: HttpClientEngine = Apache.create()) {

    val json = JSON()
    val client: HttpClient =
        HttpClient(engine) {
            expectSuccess = false
        }

    val veilarbaktivitetUrl = veilarbaktivitetConfig.url
    val veilarbaktivitetResource = veilarbaktivitetAuthenticationScope
    val poaoGcpProxyResource = poaoProxyAuthenticationScope

    suspend fun hentAktiviteter(aktorId: String, accessToken: String?): Array<Aktivitet> {
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
            val response =
                client.get<HttpResponse>("$veilarbaktivitetUrl/internal/api/v1/aktivitet?aktorId=$aktorId") {
                    header(HttpHeaders.Authorization, "Bearer ${poaoGcpProxyServiceUserAccessToken?.get()?.accessToken}")
                    header("Downstream-Authorization", "Bearer ${veilarbaktivitetOnBehalfOfAccessToken?.get()?.accessToken}")
                    header("Nav-Call-Id", IdUtils.generateId())
                    header("Nav-Consumer-Id", "veilarbapi")
                }
            if (response.status == HttpStatusCode.OK) {
                return JSON.deserialize<Array<Aktivitet>>(response.readText(), Aktivitet::class.java.arrayType())
            } else {
                throw callFailure(response)
            }

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