package no.nav.poao.veilarbapi.oppfolging

import no.nav.poao.veilarbapi.aktivitet.VeilarbaktivitetClient
import no.nav.poao.veilarbapi.dialog.VeilarbdialogClient
import no.nav.common.types.identer.AktorId
import no.nav.veilarbaktivitet.model.Aktivitet
import no.nav.veilarbapi.model.Oppfolgingsinfo
import no.nav.veilarbapi.model.OppfolgingsinfoFeilInner
import no.nav.veilarbapi.model.Oppfolgingsperioder
import no.nav.veilarbapi.model.OppfolgingsperioderFeilInner
import no.nav.veilarbapi.model.OppfolgingsperioderFeilInner.Feilkilder
import no.nav.veilarbdialog.model.Dialog

class OppfolgingService(
    val veilarbaktivitetClient: VeilarbaktivitetClient,
    val veilarbdialogClient: VeilarbdialogClient,
    val veilarboppfolgingClient: VeilarboppfolgingClient
) {

    suspend fun fetchOppfolgingsPerioder(aktorId: AktorId, accessToken: String): Oppfolgingsperioder {
        val dialoger = veilarbdialogClient.hentDialoger(aktorId, accessToken)
        val aktiviteter: Result<List<Aktivitet>> = veilarbaktivitetClient.hentAktiviteter(aktorId, accessToken)
        val oppfolgingsperioder = veilarboppfolgingClient.hentOppfolgingsperioder(aktorId, accessToken)

        val filtrerteAktiviteter = aktiviteter.getOrNull()?.filter { it.kontorsperreEnhetId == null }
        val filtrerteDialoger: List<Dialog>? = dialoger.getOrNull()
            ?.filter { it.kontorsperreEnhetId == null }
            ?.map { dialog ->
                dialog.copy(
                    henvendelser = dialog.henvendelser
                        ?.filter { henvendelse -> henvendelse.kontorsperreEnhetId == null }
                )
            }

        val response = mapOppfolgingsperioder(
            oppfolgingsperioder = oppfolgingsperioder.getOrNull(),
            aktiviteter = filtrerteAktiviteter,
            dialoger = filtrerteDialoger
        )

        val feil = listOfNotNull(
            if(dialoger.isFailure) {
                OppfolgingsperioderFeilInner(
                    feilkilder = Feilkilder.DIALOG,
                    feilmelding = dialoger.exceptionOrNull()?.message
                )
            } else null,
            if(aktiviteter.isFailure) {
                OppfolgingsperioderFeilInner(
                    feilkilder = Feilkilder.AKTIVITET,
                    feilmelding = aktiviteter.exceptionOrNull()?.message
                )
            } else null,
            if(oppfolgingsperioder.isFailure) {
                OppfolgingsperioderFeilInner(
                    feilkilder = Feilkilder.OPPFOLGING,
                    feilmelding = oppfolgingsperioder.exceptionOrNull()?.message
                )
            } else null
        )

        return response.copy(feil = feil)
    }

    suspend fun fetchOppfolgingsInfo(aktorId: AktorId, accessToken: String): Result<Oppfolgingsinfo?> {
        val erUnderOppfolging = veilarboppfolgingClient.hentErUnderOppfolging(aktorId, accessToken)
        val veileder = veilarboppfolgingClient.hentVeileder(aktorId, accessToken)
        val oppfolgingsenhet = veilarboppfolgingClient.hentOppfolgingsenhet(aktorId, accessToken)

        if (oppfolgingsenhet.isSuccess && nullOrEmpty(oppfolgingsenhet.getOrNull())) {
            return Result.success(null)
        }

        if (erUnderOppfolging.isFailure) {
            return Result.failure(erUnderOppfolging.exceptionOrNull()!!)
        }

        val oppfolgingsinfo = mapOppfolgingsinfo(erUnderOppfolging.getOrNull(), veileder.getOrNull(), oppfolgingsenhet.getOrNull())

        val feil = listOfNotNull(
            veileder.exceptionOrNull()?.let { exception ->
                OppfolgingsinfoFeilInner(
                    feilkilder = "veilederinfo",
                    feilmelding = exception.message
                )
            },
            oppfolgingsenhet.exceptionOrNull()?.let { exception ->
                OppfolgingsinfoFeilInner(
                    feilkilder = "oppfolgingsenhet",
                    feilmelding = exception.message
                )
            }
        )

        return Result.success(oppfolgingsinfo.copy(feil = feil))
    }

    private fun nullOrEmpty(oppfolgingsenhetDTO: OppfolgingsenhetDTO?): Boolean {
        if (oppfolgingsenhetDTO == null) return true
        if (oppfolgingsenhetDTO.enhetId == null) return true
        return false
    }
}