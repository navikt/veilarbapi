package no.nav.poao.veilarbapi.dialog


import VeilarbdialogClient
import com.github.michaelbull.result.get
import io.ktor.client.*
import io.ktor.client.engine.*
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
import no.nav.poao.veilarbapi.setup.http.baseEngine
import no.nav.poao.veilarbapi.setup.oauth.AzureAdClient
import no.nav.veilarbaktivitet.JSON
import no.nav.veilarbdialog.model.Dialog

class VeilarbdialogClientImpl(
    val veilarbdialogConfig: Configuration.VeilarbdialogConfig,
    val azureAdClient: AzureAdClient?,
    val engine: HttpClientEngine = baseEngine()
) : VeilarbdialogClient {

    init { JSON() }
    val client: HttpClient = baseClient(engine)
    private val veilarbdialogUrl = veilarbdialogConfig.url

    override suspend fun hentDialoger(aktorId: AktorId, accessToken: String?): Result<List<Dialog>> {

            val veilarbaktivitetOnBehalfOfAccessToken = accessToken?.let {
                azureAdClient?.getOnBehalfOfAccessTokenForResource(
                    scopes = listOf(veilarbdialogAuthenticationScope),
                    accessToken = it
                )
            }
            val poaoGcpProxyServiceUserAccessToken = accessToken?.let {
                azureAdClient?.getAccessTokenForResource(
                    scopes = listOf(poaoProxyAuthenticationScope)
                )
            }
            val response =
                client.get<HttpResponse>("$veilarbdialogUrl/internal/api/v1/dialog?aktorId=${aktorId.get()}") {
                    header(HttpHeaders.Authorization, "Bearer ${poaoGcpProxyServiceUserAccessToken?.get()?.accessToken}")
                    header("Downstream-Authorization", "Bearer ${veilarbaktivitetOnBehalfOfAccessToken?.get()?.accessToken}")
                }
            if (response.status == HttpStatusCode.OK) {
                val dialoger = JSON.deserialize<Array<Dialog>>(response.readText(), Dialog::class.java.arrayType())
                return Result.success(dialoger.toList())
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
        val veilarbdialogAuthenticationScope by lazy { "api://${if (Cluster.current == Cluster.PROD_GCP) "prod-fss" else "dev-fss"}.pto.veilarbdialog/.default" }
    }

}