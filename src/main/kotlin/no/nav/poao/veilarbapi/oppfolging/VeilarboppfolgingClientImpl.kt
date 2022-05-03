package no.nav.poao.veilarbapi.oppfolging

import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
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

class VeilarboppfolgingClientImpl(
    private val baseUrl: String,
    private val veilarboppfolgingTokenProvider: suspend (String?) -> String?,
    private val proxyTokenProvider: suspend (String?) -> String?,
    private val client: HttpClient = baseClient()
) : VeilarboppfolgingClient {

    val json = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
        .create();
    override suspend fun hentOppfolgingsperioder(aktorId: AktorId, accessToken: String?): Result<List<OppfolgingsperiodeDTO>> {
        val response =
            client.get<HttpResponse>("$baseUrl/api/v2/oppfolging/perioder?aktorId=${aktorId.get()}") {
                header(HttpHeaders.Authorization, "Bearer ${proxyTokenProvider(accessToken)}")
                header(HttpHeaders.DownstreamAuthorization, "Bearer ${veilarboppfolgingTokenProvider(accessToken)}")
            }
        if (response.status == HttpStatusCode.OK) {
            val type = object : TypeToken<List<OppfolgingsperiodeDTO>>() {}.type
            val perioder = json.fromJson<List<OppfolgingsperiodeDTO>>(response.readText(), type)

            return Result.success(perioder)
        } else {
            return Result.failure(callFailure(response))
        }
    }

    override suspend fun hentErUnderOppfolging(aktorId: AktorId, accessToken: String?): Result<UnderOppfolgingDTO> {
        val response =
            client.get<HttpResponse>("$baseUrl/api/v2/oppfolging?aktorId=${aktorId.get()}") {
                header(HttpHeaders.Authorization, "Bearer ${proxyTokenProvider(accessToken)}")
                header(HttpHeaders.DownstreamAuthorization, "Bearer ${veilarboppfolgingTokenProvider(accessToken)}")
            }

        if (response.status == HttpStatusCode.OK) {
            val underOppfolgingDTO: UnderOppfolgingDTO = json.fromJson(response.readText(), UnderOppfolgingDTO::class.java)

            return Result.success(underOppfolgingDTO)
        } else {
            return Result.failure(callFailure(response))
        }
    }

    override suspend fun hentVeileder(aktorId: AktorId, accessToken: String?): Result<VeilederDTO> {
        val response =
            client.get<HttpResponse>("$baseUrl/api/v2/veileder?aktorId=${aktorId.get()}") {
                header(HttpHeaders.Authorization, "Bearer ${proxyTokenProvider(accessToken)}")
                header(HttpHeaders.DownstreamAuthorization, "Bearer ${veilarboppfolgingTokenProvider(accessToken)}")
            }

        if (response.status == HttpStatusCode.OK) {
            val veilederDTO = json.fromJson(response.readText(), VeilederDTO::class.java)
            return Result.success(veilederDTO)
        } else {
            return Result.failure(callFailure(response))
        }
    }

    override suspend fun hentOppfolgingsenhet(aktorId: AktorId, accessToken: String?): Result<OppfolgingsenhetDTO?> {
        val response =
            client.get<HttpResponse>("$baseUrl/api/person/oppfolgingsenhet?aktorId=${aktorId.get()}") {
                header(HttpHeaders.Authorization, "Bearer ${proxyTokenProvider(accessToken)}")
                header(HttpHeaders.DownstreamAuthorization, "Bearer ${veilarboppfolgingTokenProvider(accessToken)}")
            }

        if (response.status == HttpStatusCode.OK) {
            val oppfolgingsenhetDTO = json.fromJson(response.readText(), OppfolgingsenhetDTO::class.java)

            return Result.success(oppfolgingsenhetDTO)
        } else if (response.status == HttpStatusCode.NotFound) {
            return Result.success(null)
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