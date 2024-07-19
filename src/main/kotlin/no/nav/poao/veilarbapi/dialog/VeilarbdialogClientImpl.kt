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
import no.nav.veilarbdialog.JSON
import no.nav.veilarbdialog.model.Dialog

class VeilarbdialogClientImpl(
    private val baseUrl: String,
    private val veilarbdialogTokenProvider: suspend (String?) -> String?,
    private val client: HttpClient = baseClient()
) : VeilarbdialogClient {

    init { JSON() }

    override suspend fun hentDialoger(aktorId: AktorId, accessToken: String?): Result<List<Dialog>> {
        val response =
            client.get("$baseUrl/internal/api/v1/dialog?aktorId=${aktorId.get()}") {
                header(HttpHeaders.Authorization, "Bearer ${veilarbdialogTokenProvider(accessToken)}")
            }
        if (response.status == HttpStatusCode.OK) {
            val dialoger = JSON.deserialize<Array<Dialog>>(response.bodyAsText(), Dialog::class.java.arrayType())
            return Result.success(dialoger.toList())
        } else {
            return Result.failure(callFailure(response))
        }
    }

    private suspend fun callFailure(response: HttpResponse): Exception {
        return when (response.status) {
            HttpStatusCode.Forbidden -> ManglerTilgangException(response, response.bodyAsText())
            HttpStatusCode.Unauthorized -> IkkePaaLoggetException(response, response.bodyAsText())
            HttpStatusCode.InternalServerError -> ServerFeilException(response, response.bodyAsText())
            else -> Exception("Ukjent statuskode ${response.status}")
        }
    }
}