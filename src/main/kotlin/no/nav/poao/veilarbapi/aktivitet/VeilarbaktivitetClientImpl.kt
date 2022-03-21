package no.nav.poao.veilarbapi.aktivitet


import com.github.michaelbull.result.get
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.nav.common.types.identer.AktorId
import no.nav.poao.veilarbapi.setup.config.Cluster
import no.nav.poao.veilarbapi.setup.config.Configuration
import no.nav.poao.veilarbapi.setup.exceptions.IkkePaaLoggetException
import no.nav.poao.veilarbapi.setup.exceptions.ManglerTilgangException
import no.nav.poao.veilarbapi.setup.exceptions.ServerFeilException
import no.nav.poao.veilarbapi.setup.http.baseClient
import no.nav.poao.veilarbapi.setup.oauth.AzureAdClient
import no.nav.veilarbaktivitet.JSON
import no.nav.veilarbaktivitet.model.Aktivitet

class VeilarbaktivitetClientImpl (
    val veilarbaktivitetConfig: Configuration.VeilarbaktivitetConfig,
    val azureAdClient: AzureAdClient?,
    val client: HttpClient = baseClient()
) : VeilarbaktivitetClient {

    init { JSON() }

    private val veilarbaktivitetUrl = veilarbaktivitetConfig.url

    override suspend fun hentAktiviteter(aktorId: AktorId, accessToken: String?): Result<List<Aktivitet>> {
        val veilarbaktivitetOnBehalfOfAccessToken = accessToken?.let {
            azureAdClient?.getOnBehalfOfAccessTokenForResource(
                scopes = listOf(veilarbaktivitetAuthenticationScope),
                accessToken = it
            )
        }
        val poaoGcpProxyServiceUserAccessToken = accessToken?.let {
            azureAdClient?.getAccessTokenForResource(
                scopes = listOf(poaoProxyAuthenticationScope)
            )
        }

        val response =
            client.get<HttpResponse>("$veilarbaktivitetUrl/internal/api/v1/aktivitet?aktorId=${aktorId.get()}") {
                header(HttpHeaders.Authorization, "Bearer ${poaoGcpProxyServiceUserAccessToken?.get()?.accessToken}")
                header("Downstream-Authorization", "Bearer ${veilarbaktivitetOnBehalfOfAccessToken?.get()?.accessToken}")
            }
        if (response.status == HttpStatusCode.OK) {
            val aktiviteter = JSON.deserialize<Array<Aktivitet>?>(response.readText(), Aktivitet::class.java.arrayType())
            return Result.success(aktiviteter.toList())
        } else {
            return Result.failure(callFailure(response))
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