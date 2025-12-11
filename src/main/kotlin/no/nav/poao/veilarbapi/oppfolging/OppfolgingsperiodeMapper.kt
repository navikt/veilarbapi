package no.nav.poao.veilarbapi.oppfolging

import no.nav.poao.veilarbapi.aktivitet.InternAktivitet
import no.nav.poao.veilarbapi.aktivitet.mapAktiviteter
import no.nav.poao.veilarbapi.dialog.InternDialog
import no.nav.poao.veilarbapi.dialog.mapDialoger
import no.nav.veilarbapi.model.Oppfolgingsperiode
import no.nav.veilarbapi.model.Oppfolgingsperioder

internal fun mapOppfolgingsperioder(oppfolgingsperioder: List<OppfolgingsperiodeDTO>?, aktiviteter: List<InternAktivitet>?, dialoger: List<InternDialog>?): Oppfolgingsperioder {
    val mappedOppfolgingsperioder = oppfolgingsperioder?.map { o ->
        mapOppfolgingsperiode(
            o,
            aktiviteter?.filter { a -> a.oppfolgingsperiodeId == o.uuid },
            dialoger?.filter { d -> d.oppfolgingsperiodeId == o.uuid }
        )
    } ?: run {
        if (aktiviteter.isNullOrEmpty() && dialoger.isNullOrEmpty()) {
            listOf()
        } else {
            listOf(
                mapOppfolgingsperiode(
                    OppfolgingsperiodeDTO(startDato = null, sluttDato = null),
                    aktiviteter,
                    dialoger
                )
            )
        }
    }

    return Oppfolgingsperioder().apply {
        oppfolgingsperioder(mappedOppfolgingsperioder)
    }
}

private fun mapOppfolgingsperiode(oppfolgingsperiode: OppfolgingsperiodeDTO, aktiviteter: List<InternAktivitet>?, dialoger: List<InternDialog>?): Oppfolgingsperiode {
    val partition = dialoger?.partition { it.aktivitetId == null }
    val dialogerUtenAktiviteter = partition?.first ?: emptyList()
    val dialogerMedAktiviteter = partition?.second ?: emptyList()

    val (mappedAktiviteter, dialogerPaaBortfiltrerteAktiviteter) = mapAktiviteter(aktiviteter, dialogerMedAktiviteter)

    val mappedDialoger = mapDialoger(dialogerUtenAktiviteter) + mapDialoger(dialogerPaaBortfiltrerteAktiviteter)

    return Oppfolgingsperiode().apply {
        startDato = oppfolgingsperiode.startDato
        sluttDato = oppfolgingsperiode.sluttDato
        dialoger(mappedDialoger)
        aktiviteter(mappedAktiviteter)
    }
}
