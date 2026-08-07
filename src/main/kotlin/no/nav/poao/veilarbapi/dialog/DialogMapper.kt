package no.nav.poao.veilarbapi.dialog

import no.nav.veilarbapi.model.*
import no.nav.veilarbdialog.model.AvsenderType
import no.nav.veilarbdialog.model.Henvendelse

typealias InternDialog = no.nav.veilarbdialog.model.Dialog

internal fun mapDialoger(dialoger: List<InternDialog>): List<Dialog> {
    return dialoger.map {
        mapDialog(it)
    }
}

internal fun mapDialog(dialog: InternDialog): Dialog {
    return Dialog(
        dialogStart = dialog.opprettetDato,
        tittel = dialog.overskrift,
        ventersvarnav = dialog.venterSvarNav,
        ventersvarbruker = dialog.venterSvarBruker,
        meldinger = map(dialog.henvendelser)
    )
}

private fun map(henvendelser: List<Henvendelse>?): List<Melding>? {
    return henvendelser?.map { henvendelse ->
        Melding(
            avsenderType = map(henvendelse.avsenderType),
            sendtDato = henvendelse.sendtDato,
            meldingstekst = henvendelse.tekst,
            lest = henvendelse.lestAvBruker
                .takeIf { henvendelse.avsenderType == AvsenderType.BRUKER }
                ?: henvendelse.lestAvVeileder
        )
    }
}

private fun map(avsendertype: AvsenderType?): Melding.AvsenderType? {
    return when (avsendertype) {
        AvsenderType.BRUKER -> Melding.AvsenderType.BRUKER
        AvsenderType.VEILEDER -> Melding.AvsenderType.NAV
        null -> null
    }
}
