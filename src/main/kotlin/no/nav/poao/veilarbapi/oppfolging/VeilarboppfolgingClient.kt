package no.nav.poao.veilarbapi.oppfolging

import com.expediagroup.graphql.client.types.GraphQLClientResponse
import kotlinx.serialization.Serializable
import no.nav.common.types.identer.AktorId
import no.nav.common.types.identer.Fnr
import no.nav.http.graphql.generated.client.HentOppfolgingsDataQuery
import no.nav.poao.veilarbapi.oppfolging.serdes.OffsetDateTimeSerializer
import no.nav.poao.veilarbapi.oppfolging.serdes.UUIDSerializer
import java.time.OffsetDateTime
import java.util.*

interface VeilarboppfolgingClient {
    suspend fun hentOppfolgingsperioder(aktorId: AktorId, accessToken: String): Result<List<OppfolgingsperiodeDTO>>
    suspend fun hentOppfolgingsData(aktorId: AktorId, accessToken: String): Result<GraphQLClientResponse<HentOppfolgingsDataQuery.Result>>
}

@Serializable
data class OppfolgingsperiodeDTO(
    @Serializable(with = UUIDSerializer::class)
    var uuid: UUID? = null,
    var aktorId: String? = null,
    var veileder: String? = null,
    @Serializable(with = OffsetDateTimeSerializer::class)
    var startDato: OffsetDateTime? = null,
    @Serializable(with = OffsetDateTimeSerializer::class)
    var sluttDato: OffsetDateTime? = null
)

@Serializable
data class UnderOppfolgingDTO(var erUnderOppfolging: Boolean? = null)

@Serializable
data class VeilederDTO(var veilederIdent: String? = null)

@Serializable
data class OppfolgingsenhetDTO(
    var navn: String? = null,
    var enhetId: String? = null
)