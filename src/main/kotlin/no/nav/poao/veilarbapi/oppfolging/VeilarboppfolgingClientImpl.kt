package no.nav.poao.veilarbapi.oppfolging


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
import no.nav.veilarbaktivitet.JSON

class VeilarboppfolgingClientImpl(
    val veilarboppfolgingConfig: Configuration.VeilarboppfolgingConfig,
    private val veilarboppfolgingTokenProvider: suspend (String?) -> String?,
    private val proxyTokenProvider: suspend (String?) -> String?,
    val client: HttpClient = baseClient()
) : VeilarboppfolgingClient {

    init { JSON() }

    private val veilarboppfolgingUrl = veilarboppfolgingConfig.url

    override suspend fun hentOppfolgingsperioder(aktorId: AktorId, accessToken: String?): Result<List<OppfolgingsperiodeDTO>> {
        val response =
            client.get<HttpResponse>("$veilarboppfolgingUrl/api/v2/oppfolging/perioder?aktorId=${aktorId.get()}") {
                header(HttpHeaders.Authorization, "Bearer ${proxyTokenProvider(accessToken)}")
                header("Downstream-Authorization", "Bearer ${veilarboppfolgingTokenProvider(accessToken)}")
            }
        if (response.status == HttpStatusCode.OK) {
            val perioder = JSON.deserialize<Array<OppfolgingsperiodeDTO>>(
                response.readText(),
                OppfolgingsperiodeDTO::class.java.arrayType()
            ).toList()

            return Result.success(perioder)
        } else {
            return Result.failure(callFailure(response))
        }
    }

    override suspend fun hentErUnderOppfolging(aktorId: AktorId, accessToken: String?): Result<UnderOppfolgingDTO> {
        val response =
            client.get<HttpResponse>("$veilarboppfolgingUrl/api/v2/oppfolging?aktorId=${aktorId.get()}") {
                header(HttpHeaders.Authorization, "Bearer ${proxyTokenProvider(accessToken)}")
                header("Downstream-Authorization", "Bearer ${veilarboppfolgingTokenProvider(accessToken)}")
            }

        if (response.status == HttpStatusCode.OK) {
            val underOppfolgingDTO = JSON.deserialize<UnderOppfolgingDTO>(
                response.readText(),
                UnderOppfolgingDTO::class.java
            )

            return Result.success(underOppfolgingDTO)
        } else {
            return Result.failure(callFailure(response))
        }
    }

    override suspend fun hentVeileder(aktorId: AktorId, accessToken: String?): Result<VeilederDTO> {
        val response =
            client.get<HttpResponse>("$veilarboppfolgingUrl/api/v2/veileder?aktorId=${aktorId.get()}") {
                header(HttpHeaders.Authorization, "Bearer ${proxyTokenProvider(accessToken)}")
                header("Downstream-Authorization", "Bearer ${veilarboppfolgingTokenProvider(accessToken)}")
            }

        if (response.status == HttpStatusCode.OK) {
            val veilederDTO = JSON.deserialize<VeilederDTO>(
                response.readText(),
                VeilederDTO::class.java
            )
            return Result.success(veilederDTO)
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
        val veilarboppfolgingAuthenticationScope by lazy { "api://${if (Cluster.current == Cluster.PROD_GCP) "prod-fss" else "dev-fss"}.pto.veilarboppfolging/.default" }
    }

}