package no.nav.poao.veilarbapi.oppfolging


import com.expediagroup.graphql.client.ktor.GraphQLKtorClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.serialization.json.Json
import no.nav.common.types.identer.AktorId
import no.nav.http.graphql.generated.client.HentOppfolgingsDataQuery
import no.nav.http.graphql.generated.client.HentOppfolgingsPerioderQuery
import no.nav.poao.veilarbapi.oppfolging.serdes.VeilarbapiSerializerModule
import no.nav.poao.veilarbapi.setup.http.baseClient
import org.slf4j.LoggerFactory
import java.net.URI
import java.util.UUID


class VeilarboppfolgingClientImpl(
    baseUrl: String,
    private val veilarboppfolgingTokenProvider: suspend (String) -> String,
    client: HttpClient = baseClient()
) : VeilarboppfolgingClient {

    val logger = LoggerFactory.getLogger(VeilarboppfolgingClientImpl::class.java)
    val json = Json {
        serializersModule = VeilarbapiSerializerModule
        ignoreUnknownKeys = true
    }

    val graphqlClient = GraphQLKtorClient(
        url = URI.create("$baseUrl/graphql").toURL(),
        httpClient = client
    )

    override suspend fun hentOppfolgingsData(aktorId: AktorId, accessToken: String): Result<GraphQLClientResponse<HentOppfolgingsDataQuery.Result>> {
        val request = HentOppfolgingsDataQuery(HentOppfolgingsDataQuery.Variables(aktorId.toString()))

        try {
            val veilarbOppfolgingToken = veilarboppfolgingTokenProvider(accessToken)
            val response = graphqlClient.execute(request) { bearerAuth(veilarbOppfolgingToken) }
            if (response.errors != null && response.errors!!.isNotEmpty()) {
                logger.error("Feil ved graphql kall til veilarboppfolging: ${response.errors!!.joinToString { it.message }}")
            }
            return Result.success(response)
        } catch (exception: Exception) {
            return Result.failure(exception)
        }
    }

    override suspend fun hentOppfolgingsperioder(aktorId: AktorId, accessToken: String): Result<List<OppfolgingsperiodeDTO>> {
        val request = HentOppfolgingsPerioderQuery(HentOppfolgingsPerioderQuery.Variables(aktorId.toString()))

        try {
            val veilarbOppfolgingToken = veilarboppfolgingTokenProvider(accessToken)
            val response = graphqlClient.execute(request) { bearerAuth(veilarbOppfolgingToken) }
            if (response.errors != null && response.errors!!.isNotEmpty()) {
                val errorMessage = "Feil ved graphql kall til veilarboppfolging: ${response.errors!!.joinToString { it.message }}"
                logger.error(errorMessage)
                return Result.failure(Exception(errorMessage))
            } else {
                val perioder = response.data?.oppfolgingsPerioder
                    ?.map { OppfolgingsperiodeDTO(
                        UUID.fromString(it!!.id),
                        aktorId.get(),
                        null,
                        it.startTidspunkt,
                        it.sluttTidspunkt)
                    }
                    ?: emptyList()
                return Result.success(perioder)
            }
        } catch (exception: Exception) {
            return Result.failure(exception)
        }
    }
}