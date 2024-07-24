package no.nav.poao.veilarbapi.oppfolging

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import no.nav.common.types.identer.AktorId
import no.nav.poao.veilarbapi.typeadapter.OffsetDateTimeTypeAdapter
import org.threeten.bp.OffsetDateTime
import java.util.*

interface VeilarboppfolgingClient {
    suspend fun hentOppfolgingsperioder(aktorId: AktorId, accessToken: String): Result<List<OppfolgingsperiodeDTO>>

    suspend fun hentErUnderOppfolging(aktorId: AktorId, accessToken: String): Result<UnderOppfolgingDTO>

    suspend fun hentVeileder(aktorId: AktorId, accessToken: String): Result<VeilederDTO?>

    suspend fun hentOppfolgingsenhet(aktorId: AktorId, accessToken: String): Result<OppfolgingsenhetDTO?>
}

fun gson(): Gson {
    return GsonBuilder()
        .registerTypeAdapter(OffsetDateTime::class.java, OffsetDateTimeTypeAdapter())
        .create()
}


data class OppfolgingsperiodeDTO(
    var uuid: UUID? = null,
    var aktorId: String? = null,
    var veileder: String? = null,
    var startDato: OffsetDateTime? = null,
    var sluttDato: OffsetDateTime? = null
)

data class UnderOppfolgingDTO(var erUnderOppfolging: Boolean? = null)

data class VeilederDTO(var veilederIdent: String? = null)

data class OppfolgingsenhetDTO(
    var navn: String? = null,
    var enhetId: String? = null
)