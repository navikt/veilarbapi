package no.nav.poao.veilarbapi.oppfolging

import org.threeten.bp.OffsetDateTime

data class KvpPeriodeDTO(
    var opprettetDato: OffsetDateTime? = null,
    var avsluttetDato: OffsetDateTime? = null
)
