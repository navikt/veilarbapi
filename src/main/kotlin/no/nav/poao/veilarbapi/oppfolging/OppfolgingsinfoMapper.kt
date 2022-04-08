package no.nav.poao.veilarbapi.oppfolging

import no.nav.veilarbapi.model.Oppfolgingsinfo

internal fun mapOppfolgingsinfo(
    underOppfolgingDTO: UnderOppfolgingDTO?,
    veilederDTO: VeilederDTO? = null,
    orNull: OppfolgingsenhetDTO?
): Oppfolgingsinfo {
    return Oppfolgingsinfo().apply {
        underOppfolging = underOppfolgingDTO?.erUnderOppfolging
        primaerVeileder = veilederDTO?.veilederIdent?.get()
    }
}