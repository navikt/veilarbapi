package no.nav.poao.veilarbapi.dialog


import com.github.michaelbull.result.get
import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.apache.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.nav.common.types.identer.AktorId
import no.nav.common.utils.IdUtils
import no.nav.poao.veilarbapi.settup.oauth.AzureAdClient
import no.nav.poao.veilarbapi.settup.exceptions.IkkePaaLoggetException
import no.nav.poao.veilarbapi.settup.exceptions.ManglerTilgangException
import no.nav.poao.veilarbapi.settup.exceptions.ServerFeilException
import no.nav.veilarbaktivitet.JSON
import no.nav.poao.veilarbapi.settup.config.Cluster
import no.nav.poao.veilarbapi.settup.config.Configuration
import no.nav.veilarbdialog.model.Dialog
import org.slf4j.MDC

class VeilarbdialogClient constructor(val veilarbdialogConfig: Configuration.VeilarbdialogConfig, val azureAdClient: AzureAdClient?, val engine: HttpClientEngine = Apache.create()) {

    val json = JSON()
    val client: HttpClient =
        HttpClient(engine) {
            expectSuccess = false
        }

    val veilarbdialogUrl = veilarbdialogConfig.url


    suspend fun hentDialoger(aktorId: AktorId, accessToken: String?): Result<List<Dialog>> {

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
                    header("Nav-Call-Id", MDC.get("Nav-Call-Id") ?: IdUtils.generateId())
                    header("Nav-Consumer-Id", "veilarbapi")
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