package no.nav.poao.veilarbapi.oppfolging

import no.nav.common.types.identer.AktorId
import no.nav.poao.veilarbapi.aktivitet.VeilarbaktivitetClient
import no.nav.poao.veilarbapi.dialog.VeilarbdialogClient
import no.nav.veilarbapi.model.Oppfolgingsinfo
import no.nav.veilarbapi.model.Oppfolgingsperioder
import no.nav.veilarbapi.model.OppfolgingsperioderFeil
import no.nav.veilarbapi.model.OppfolgingsperioderFeil.FeilkilderEnum

class Service(val aktivitet: VeilarbaktivitetClient, val dialog: VeilarbdialogClient, val oppfolging: VeilarboppfolgingClient) {
    suspend fun fetchOppfolgingsPerioder(aktorId: AktorId, accessToken: String?): Oppfolgingsperioder {
        val dialoger = dialog.hentDialoger(aktorId, accessToken)
        val aktiviteter = aktivitet.hentAktiviteter(aktorId, accessToken)
        val oppfolgingsperioder = oppfolging.hentOppfolgingsperioder(aktorId, accessToken)


        val response = mapOppfolgingsperioder(
            oppfolgingsperioder = oppfolgingsperioder.getOrNull(),
            aktiviteter = aktiviteter.getOrNull(),
            dialoger = dialoger.getOrNull()
        )

        if(dialoger.isFailure) {
            val feil = OppfolgingsperioderFeil().apply {
                feilkilder = FeilkilderEnum.DIALOG
                feilmelding = dialoger.exceptionOrNull()?.message
            }
            response.addFeilItem(feil)
        }
        if(aktiviteter.isFailure) {
            val feil = OppfolgingsperioderFeil().apply {
                feilkilder = FeilkilderEnum.AKTIVITET
                feilmelding = aktiviteter.exceptionOrNull()?.message
            }
            response.addFeilItem(feil)
        }
        if(oppfolgingsperioder.isFailure) {
            val feil = OppfolgingsperioderFeil().apply {
                feilkilder = FeilkilderEnum.OPPFOLGING
                feilmelding = oppfolgingsperioder.exceptionOrNull()?.message
            }
            response.addFeilItem(feil)
        }
        return response
    }

    suspend fun fetchOppfolgingsInfo(aktorId: AktorId, accessToken: String?): Oppfolgingsinfo {
        val erUnderOppfolging = oppfolging.hentErUnderOppfolging(aktorId, accessToken)
        val veileder = oppfolging.hentVeileder(aktorId, accessToken)

        if (erUnderOppfolging.isFailure) {
            // ikke returner veileder
            // tomt objekt eller feilmelding?
            return Oppfolgingsinfo() //TODO fiks dette
        }

        if (veileder.isFailure) {
            return mapOppfolgingsInfo(erUnderOppfolging.getOrNull())
        }

        return mapOppfolgingsInfo(erUnderOppfolging.getOrNull(), veileder.getOrNull())
    }
}