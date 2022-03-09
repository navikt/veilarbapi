package no.nav.poao.veilarbapi.oppfolging

import no.nav.veilarbapi.model.Oppfolgingsinfo

internal fun mapOppfolgingsInfo(underOppfolgingDTO: UnderOppfolgingDTO?, veilederDTO: VeilederDTO? = null): Oppfolgingsinfo {
    return Oppfolgingsinfo().apply {
        underOppfolging = underOppfolgingDTO?.erUnderOppfolging
        primaerVeileder = veilederDTO?.veilederIdent?.get()
    }
}