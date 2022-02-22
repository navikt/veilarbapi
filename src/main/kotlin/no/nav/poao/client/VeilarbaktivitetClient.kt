package no.nav.poao.client


import io.ktor.client.*
import io.ktor.client.engine.*
import io.ktor.client.engine.java.*
import io.ktor.client.features.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import no.nav.common.sts.NaisSystemUserTokenProvider
import no.nav.common.utils.IdUtils
import no.nav.poao.client.exceptions.IkkePaaLoggetException
import no.nav.poao.client.exceptions.ManglerTilgangException
import no.nav.poao.client.exceptions.ServerFeilException
import no.nav.veilarbaktivitet.JSON
import no.nav.veilarbaktivitet.model.Aktivitet
import no.nav.poao.config.Cluster

class VeilarbaktivitetClient constructor(val veilarbaktivitetUrl: String, val systemUserTokenProvider: NaisSystemUserTokenProvider?, engine: HttpClientEngine = Java.create()) {

    val json = JSON()
    val client: HttpClient =
        HttpClient(engine) {
            expectSuccess = false
            defaultRequest {
                mandatoryHeaders()
            }
        }

    fun hentAktivitet(aktivitetsId: Int): Aktivitet? {
        return runBlocking {
            client.use { httpClient ->
                val response =
                httpClient.get<HttpResponse>("$veilarbaktivitetUrl/internal/api/v1/aktivitet/$aktivitetsId")
                if (response.status == HttpStatusCode.OK) {
                        Aktivitet.fromJson(response.readText())
                } else {
                    throw callFailure(response)
                }
            }
        }
    }

    fun hentAktiviteter(aktorId: String): Array<Aktivitet>? {
        return runBlocking {
            client.use { httpClient ->
                val response =
                    httpClient.get<HttpResponse>("$veilarbaktivitetUrl/internal/api/v1/aktivitet?aktorId=$aktorId")
                if (response.status == HttpStatusCode.OK) {
                    JSON.deserialize<Array<Aktivitet>>(response.readText(), Aktivitet::class.java.arrayType())
                } else {
                    throw callFailure(response)
                }
            }
        }
    }


    fun HttpRequestBuilder.mandatoryHeaders() {
        header("Nav-Call-Id", IdUtils.generateId())
        header("Nav-Consumer-Id", "veilarbapi")
        if (systemUserTokenProvider != null) {
            header("Authorization", "Bearer " + systemUserTokenProvider.systemUserToken)
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
        val ptoProxyAuthenticationScope by lazy { "api://${if (Cluster.current == Cluster.PROD_GCP) "prod-gcp" else "dev-gcp"}.pto-proxy/.default" }
    }
}