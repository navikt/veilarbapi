package no.nav.poao.veilarbapi.oppfolging

import no.nav.veilarbapi.model.Oppfolgingsinfo
import no.nav.veilarbapi.model.OppfolgingsinfoOppfolgingsEnhet

internal fun mapOppfolgingsinfo(
    underOppfolgingDTO: UnderOppfolgingDTO?,
    veilederDTO: VeilederDTO? = null,
    oppfolgingsenhetDTO: OppfolgingsenhetDTO?
): Oppfolgingsinfo {
    return Oppfolgingsinfo().apply {
        underOppfolging = underOppfolgingDTO?.erUnderOppfolging
        primaerVeileder = veilederDTO?.veilederIdent
        oppfolgingsEnhet = OppfolgingsinfoOppfolgingsEnhet().apply {
            enhetId = oppfolgingsenhetDTO?.enhetId
            navn = oppfolgingsenhetDTO?.navn
        }
    }
}