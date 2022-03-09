package no.nav.poao.veilarbapi.oppfolging


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
import org.slf4j.MDC

class VeilarboppfolgingClient constructor(
    val veilarboppfolgingConfig: Configuration.VeilarboppfolgingConfig,
    val azureAdClient: AzureAdClient?,
    val engine: HttpClientEngine = Apache.create()
) {

    val json = JSON()
    val client: HttpClient =
        HttpClient(engine) {
            expectSuccess = false
        }

    val veilarboppfolgingUrl = veilarboppfolgingConfig.url

    suspend fun hentOppfolgingsperioder(aktorId: AktorId, accessToken: String?): Result<List<OppfolgingsperiodeDTO>> {
        val veilarboppfolgingOnBehalfOfAccessToken = accessToken?.let {
            azureAdClient?.getOnBehalfOfAccessTokenForResource(
                scopes = listOf(veilaroppfolgingAuthenticationScope),
                accessToken = it
            )
        }
        val poaoGcpProxyServiceUserAccessToken = accessToken?.let {
            azureAdClient?.getAccessTokenForResource(
                scopes = listOf(poaoProxyAuthenticationScope)
            )
        }

        val response =
            client.get<HttpResponse>("$veilarboppfolgingUrl/api/v2/oppfolging/perioder?aktorId=${aktorId.get()}") {
                header(HttpHeaders.Authorization, "Bearer ${poaoGcpProxyServiceUserAccessToken?.get()?.accessToken}")
                header(
                    "Downstream-Authorization",
                    "Bearer ${veilarboppfolgingOnBehalfOfAccessToken?.get()?.accessToken}"
                )
                header("Nav-Call-Id", MDC.get("Nav-Call-Id") ?: IdUtils.generateId())
                header("Nav-Consumer-Id", "veilarbapi")
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

    private suspend fun callFailure(response: HttpResponse): Exception {
        return when (response.status) {
            HttpStatusCode.Forbidden -> ManglerTilgangException(response, response.readText())
            HttpStatusCode.Unauthorized -> IkkePaaLoggetException(response, response.readText())
            HttpStatusCode.InternalServerError -> ServerFeilException(response, response.readText())
            else -> Exception("Ukjent statuskode ${response.status}")
        }
    }

    suspend fun hentErUnderOppfolging(aktorId: AktorId, accessToken: String?): Result<UnderOppfolgingDTO> {
        val veilarboppfolgingOnBehalfOfAccessToken = accessToken?.let {
            azureAdClient?.getOnBehalfOfAccessTokenForResource(
                scopes = listOf(veilaroppfolgingAuthenticationScope),
                accessToken = it
            )
        }
        val poaoGcpProxyServiceUserAccessToken = accessToken?.let {
            azureAdClient?.getAccessTokenForResource(
                scopes = listOf(poaoProxyAuthenticationScope)
            )
        }

        val response =
            client.get<HttpResponse>("$veilarboppfolgingUrl/api/v2/oppfolging?aktorId=${aktorId.get()}") {
                header(HttpHeaders.Authorization, "Bearer ${poaoGcpProxyServiceUserAccessToken?.get()?.accessToken}")
                header(
                    "Downstream-Authorization",
                    "Bearer ${veilarboppfolgingOnBehalfOfAccessToken?.get()?.accessToken}"
                )
                header("Nav-Call-Id", MDC.get("Nav-Call-Id") ?: IdUtils.generateId())
                header("Nav-Consumer-Id", "veilarbapi")
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

    suspend fun hentVeileder(aktorId: AktorId, accessToken: String?): Result<VeilederDTO> {
        val veilarboppfolgingOnBehalfOfAccessToken = accessToken?.let {
            azureAdClient?.getOnBehalfOfAccessTokenForResource(
                scopes = listOf(veilaroppfolgingAuthenticationScope),
                accessToken = it
            )
        }
        val poaoGcpProxyServiceUserAccessToken = accessToken?.let {
            azureAdClient?.getAccessTokenForResource(
                scopes = listOf(poaoProxyAuthenticationScope)
            )
        }

        val response =
            client.get<HttpResponse>("$veilarboppfolgingUrl/api/v2/veileder?aktorId=${aktorId.get()}") {
                header(HttpHeaders.Authorization, "Bearer ${poaoGcpProxyServiceUserAccessToken?.get()?.accessToken}")
                header(
                    "Downstream-Authorization",
                    "Bearer ${veilarboppfolgingOnBehalfOfAccessToken?.get()?.accessToken}"
                )
                header("Nav-Call-Id", MDC.get("Nav-Call-Id") ?: IdUtils.generateId())
                header("Nav-Consumer-Id", "veilarbapi")
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


    companion object {
        val poaoProxyAuthenticationScope by lazy { "api://${if (Cluster.current == Cluster.PROD_GCP) "prod-fss" else "dev-fss"}.pto.poao-gcp-proxy/.default" }
        val veilaroppfolgingAuthenticationScope by lazy { "api://${if (Cluster.current == Cluster.PROD_GCP) "prod-fss" else "dev-fss"}.pto.veilarboppfolging/.default" }
    }

}