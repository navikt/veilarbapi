package no.nav.poao.veilarbapi.dialog


import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import no.nav.common.types.identer.AktorId
import no.nav.poao.veilarbapi.oppfolging.serdes.VeilarbapiSerializerModule
import no.nav.poao.veilarbapi.setup.exceptions.IkkePaaLoggetException
import no.nav.poao.veilarbapi.setup.exceptions.ManglerTilgangException
import no.nav.poao.veilarbapi.setup.exceptions.EksternServerFeilException
import no.nav.poao.veilarbapi.setup.http.baseClient
import no.nav.veilarbdialog.model.Dialog
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger(VeilarbdialogClientImpl::class.java)

class VeilarbdialogClientImpl(
    private val baseUrl: String,
    private val veilarbdialogTokenProvider: suspend (String) -> String,
    private val client: HttpClient = baseClient()
) : VeilarbdialogClient {

    val json = Json { serializersModule = VeilarbapiSerializerModule }

    override suspend fun hentDialoger(aktorId: AktorId, accessToken: String): Result<List<Dialog>> {
        val response =
            client.get("$baseUrl/internal/api/v1/dialog?aktorId=${aktorId.get()}") {
                header(HttpHeaders.Authorization, "Bearer ${veilarbdialogTokenProvider(accessToken)}")
            }
        if (response.status == HttpStatusCode.OK) {
            val dialoger = json.decodeFromString<List<Dialog>>(response.bodyAsText())
            return Result.success(dialoger.toList())
        } else {
            logger.error("Feilet å hente dialog data", response.toString())
            return Result.failure(callFailure(response))
        }
    }

    private suspend fun callFailure(response: HttpResponse): Exception {
        return when (response.status) {
            HttpStatusCode.Forbidden -> ManglerTilgangException(response, response.bodyAsText())
            HttpStatusCode.Unauthorized -> IkkePaaLoggetException(response, response.bodyAsText())
            HttpStatusCode.InternalServerError -> EksternServerFeilException(response, response.bodyAsText())
            else -> Exception("Ukjent statuskode ${response.status}")
        }
    }
}