package no.nav.poao.veilarbapi.dialog


import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import no.nav.common.types.identer.AktorId
import no.nav.poao.veilarbapi.setup.exceptions.IkkePaaLoggetException
import no.nav.poao.veilarbapi.setup.exceptions.ManglerTilgangException
import no.nav.poao.veilarbapi.setup.exceptions.ServerFeilException
import no.nav.poao.veilarbapi.setup.http.DownstreamAuthorization
import no.nav.poao.veilarbapi.setup.http.baseClient
import no.nav.veilarbaktivitet.JSON
import no.nav.veilarbdialog.model.Dialog

class VeilarbdialogClientImpl(
    private val baseUrl: String,
    private val veilarbdialogTokenProvider: suspend (String?) -> String?,
    private val proxyTokenProvider: suspend (String?) -> String?,
    private val client: HttpClient = baseClient()
) : VeilarbdialogClient {

    init { JSON() }

    override suspend fun hentDialoger(aktorId: AktorId, accessToken: String?): Result<List<Dialog>> {
        val response =
            client.get<HttpResponse>("$baseUrl/internal/api/v1/dialog?aktorId=${aktorId.get()}") {
                header(HttpHeaders.Authorization, "Bearer ${proxyTokenProvider(accessToken)}")
                header(HttpHeaders.DownstreamAuthorization, "Bearer ${veilarbdialogTokenProvider(accessToken)}")
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
}