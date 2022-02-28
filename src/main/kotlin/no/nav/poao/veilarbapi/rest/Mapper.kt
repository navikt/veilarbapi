package no.nav.poao.veilarbapi.rest

import no.nav.veilarbapi.model.*
import no.nav.veilarbdialog.model.Henvendelse

typealias InternDialog = no.nav.veilarbdialog.model.Dialog

class Mapper {
    fun mapb(dialog: InternDialog): Dialog {
        return Dialog().apply {
            dialogStart = dialog.opprettetDato
            tittel = dialog.overskrift
            ventersvarbruker = dialog.venterSvarNav
            ventersvarbruker = dialog.venterSvarBruker
            meldinger = map(dialog.henvendelser)
        }
    }

    private fun map(henvendelser: List<Henvendelse>?): List<Melding>? {
        return henvendelser?.map {
            Melding().apply {
                avsenderType = map(it.avsenderType)
                sendtDato = it.sendtDato
                meldingstekst = it.tekst
                lest = createLest(
                    it.avsenderType,
                    it.lestAvBruker,
                    it.lestAvVeileder
                )
            }
        }
    }

    private fun createLest(
        avsenderType: Henvendelse.AvsenderTypeEnum?,
        lestAvBruker: Boolean?,
        lestAvVeileder: Boolean?
    ): Boolean? {
        return lestAvBruker.takeIf { avsenderType!! == Henvendelse.AvsenderTypeEnum.BRUKER } ?: lestAvVeileder
    }

    private fun map(henvendelser: Henvendelse.AvsenderTypeEnum?): Melding.AvsenderTypeEnum? {

    }


}