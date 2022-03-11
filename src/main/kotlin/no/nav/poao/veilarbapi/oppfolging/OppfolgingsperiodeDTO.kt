package no.nav.poao.veilarbapi.oppfolging

import org.threeten.bp.OffsetDateTime
import java.util.*

data class OppfolgingsperiodeDTO(
    var uuid: UUID? = null,
    var aktorId: String? = null,
    var veileder: String? = null,
    var startDato: OffsetDateTime? = null,
    var sluttDato: OffsetDateTime? = null,
    var kvpPerioder: List<KvpPeriodeDTO?>? = null
)

