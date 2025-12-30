package no.nav.poao.veilarbapi.oppfolging


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
import org.slf4j.LoggerFactory


class VeilarboppfolgingClientImpl(
    private val baseUrl: String,
    private val veilarboppfolgingTokenProvider: suspend (String) -> String,
    private val client: HttpClient = baseClient()
) : VeilarboppfolgingClient {

    val logger = LoggerFactory.getLogger(VeilarboppfolgingClientImpl::class.java)
    val json = Json {
        serializersModule = VeilarbapiSerializerModule
        ignoreUnknownKeys = true
    }

    override suspend fun hentOppfolgingsperioder(aktorId: AktorId, accessToken: String): Result<List<OppfolgingsperiodeDTO>> {
        val response =
            client.get("$baseUrl/api/v2/oppfolging/perioder?aktorId=${aktorId.get()}") {
                header(HttpHeaders.Authorization, "Bearer ${veilarboppfolgingTokenProvider(accessToken)}")
            }

        if (response.status == HttpStatusCode.OK) {
            val perioder = json.decodeFromString<List<OppfolgingsperiodeDTO>>(response.bodyAsText())

            return Result.success(perioder)
        } else {
            logger.error("Kunne ikke hente oppfolgingsperioder fra veilarboppfolging: ${response.status} ${response.bodyAsText()}}")
            return Result.failure(callFailure(response))
        }
    }

    override suspend fun hentErUnderOppfolging(aktorId: AktorId, accessToken: String): Result<UnderOppfolgingDTO> {
        val response =
            client.get("$baseUrl/api/v2/oppfolging?aktorId=${aktorId.get()}") {
                header(HttpHeaders.Authorization, "Bearer ${veilarboppfolgingTokenProvider(accessToken)}")
            }

        if (response.status == HttpStatusCode.OK) {
            val underOppfolgingDTO = json.decodeFromString<UnderOppfolgingDTO>(response.bodyAsText())

            return Result.success(underOppfolgingDTO)
        } else {
            return Result.failure(callFailure(response))
        }
    }

    override suspend fun hentVeileder(aktorId: AktorId, accessToken: String): Result<VeilederDTO?> {
        val response =
            client.get("$baseUrl/api/v2/veileder?aktorId=${aktorId.get()}") {
                header(HttpHeaders.Authorization, "Bearer ${veilarboppfolgingTokenProvider(accessToken)}")
            }

        if (response.status == HttpStatusCode.OK) {
            val veilederDTO = json.decodeFromString<VeilederDTO>(response.bodyAsText())
            return Result.success(veilederDTO)
        } else if (response.status == HttpStatusCode.NoContent) {
            return Result.success(null)
        } else {
            logger.error("Kunne ikke hente veileder fra veilarboppfolging: ${response.status} ${response.bodyAsText()}")
            return Result.failure(callFailure(response))
        }
    }

    override suspend fun hentOppfolgingsenhet(aktorId: AktorId, accessToken: String): Result<OppfolgingsenhetDTO?> {
        val response =
            client.get("$baseUrl/api/person/oppfolgingsenhet?aktorId=${aktorId.get()}") {
                header(HttpHeaders.Authorization, "Bearer ${veilarboppfolgingTokenProvider(accessToken)}")
            }

        if (response.status == HttpStatusCode.OK) {
            val oppfolgingsenhetDTO = json.decodeFromString<OppfolgingsenhetDTO>(response.bodyAsText())

            return Result.success(oppfolgingsenhetDTO)
        } else if (response.status == HttpStatusCode.NotFound) {
            return Result.success(null)
        } else {
            logger.error("Kunne ikke hente oppfolgingsenhet fra veilarboppfolging: ${response.status} ${response.bodyAsText()}}")
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