package no.nav.poao.veilarbapi.dialog

import no.nav.veilarbapi.model.*
import no.nav.veilarbdialog.model.Henvendelse

typealias InternDialog = no.nav.veilarbdialog.model.Dialog

internal fun mapDialoger(dialoger: List<InternDialog>?): List<Dialog>? {
    return dialoger?.map {
        mapDialog(it)
    }
}

internal fun mapDialog(dialog: InternDialog): Dialog {
    return Dialog().apply {
        dialogStart = dialog.opprettetDato
        tittel = dialog.overskrift
        ventersvarbruker = dialog.venterSvarNav
        ventersvarbruker = dialog.venterSvarBruker
        meldinger = map(dialog.henvendelser)
    }
}

private fun map(henvendelser: List<Henvendelse>?): List<Melding>? {
    return henvendelser?.map { henvendelse ->
        Melding().apply {
            avsenderType = map(henvendelse.avsenderType)
            sendtDato = henvendelse.sendtDato
            meldingstekst = henvendelse.tekst
            lest = henvendelse.lestAvBruker.takeIf { henvendelse.avsenderType == Henvendelse.AvsenderTypeEnum.BRUKER } ?: henvendelse.lestAvVeileder
        }
    }
}

private fun map(avsendertype: Henvendelse.AvsenderTypeEnum?): Melding.AvsenderTypeEnum? {
    return when (avsendertype) {
        Henvendelse.AvsenderTypeEnum.BRUKER -> Melding.AvsenderTypeEnum.BRUKER
        Henvendelse.AvsenderTypeEnum.VEILEDER -> Melding.AvsenderTypeEnum.NAV
        null -> null
    }
}
