package no.nav.poao.veilarbapi.oppfolging

import no.nav.common.types.identer.AktorId
import no.nav.poao.veilarbapi.aktivitet.VeilarbaktivitetClient
import no.nav.poao.veilarbapi.dialog.VeilarbdialogClient
import no.nav.veilarbaktivitet.model.Aktivitet
import no.nav.veilarbapi.model.*
import no.nav.veilarbapi.model.OppfolgingsperioderFeil.FeilkilderEnum
import no.nav.veilarbdialog.model.Dialog

class Service(val aktivitet: VeilarbaktivitetClient, val dialog: VeilarbdialogClient, val oppfolging: VeilarboppfolgingClient) {
    suspend fun fetchOppfolgingsPerioder(aktorId: AktorId, accessToken: String?): Oppfolgingsperioder {
        val dialoger = dialog.hentDialoger(aktorId, accessToken)
        val aktiviteter: Result<List<Aktivitet>> = aktivitet.hentAktiviteter(aktorId, accessToken)
        val oppfolgingsperioder = oppfolging.hentOppfolgingsperioder(aktorId, accessToken)

        val filtrerteAktiviteter = aktiviteter.getOrNull()?.filter { it.kontorsperreEnhetId == null }
        val filtrerteDialoger: List<Dialog>? = dialoger.getOrNull()
            ?.filter { it.kontorsperreEnhetId == null }
            ?.map { dialog ->
                dialog.henvendelser = dialog.henvendelser?.filter { henvendelse -> henvendelse.kontorsperreEnhetId == null }
                dialog
            }

        val response = mapOppfolgingsperioder(
            oppfolgingsperioder = oppfolgingsperioder.getOrNull(),
            aktiviteter = filtrerteAktiviteter,
            dialoger = filtrerteDialoger
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

    suspend fun fetchOppfolgingsInfo(aktorId: AktorId, accessToken: String?): Result<Oppfolgingsinfo> {
        val erUnderOppfolging = oppfolging.hentErUnderOppfolging(aktorId, accessToken)
        val veileder = oppfolging.hentVeileder(aktorId, accessToken)

        if (erUnderOppfolging.isFailure) {
            return Result.failure(erUnderOppfolging.exceptionOrNull()!!)
        }

        if (veileder.isFailure) {
            val oppfolgingsinfo = mapOppfolgingsInfo(erUnderOppfolging.getOrNull())
            val feil = OppfolgingsinfoFeil().apply {
                feilkilder = "veilederinfo"
                feilmelding = veileder.exceptionOrNull()?.message
            }
            oppfolgingsinfo.addFeilItem(feil)
            return Result.success(oppfolgingsinfo)
        }

        return Result.success(mapOppfolgingsInfo(erUnderOppfolging.getOrNull(), veileder.getOrNull()))
    }
}