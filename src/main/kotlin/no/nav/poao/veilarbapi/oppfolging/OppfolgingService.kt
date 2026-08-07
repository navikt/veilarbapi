package no.nav.poao.veilarbapi.oppfolging

import no.nav.poao.veilarbapi.aktivitet.VeilarbaktivitetClient
import no.nav.poao.veilarbapi.dialog.VeilarbdialogClient
import no.nav.common.types.identer.AktorId
import no.nav.veilarbaktivitet.model.Aktivitet
import no.nav.veilarbapi.model.Oppfolgingsinfo
import no.nav.veilarbapi.model.OppfolgingsinfoFeilInner
import no.nav.veilarbapi.model.OppfolgingsinfoOppfolgingsEnhet
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
        val result = veilarboppfolgingClient.hentOppfolgingsData(aktorId, accessToken)

        if (result.isFailure) return Result.failure(result.exceptionOrNull()!!)
        val oppfolgingsData = result.getOrNull()
            ?: return Result.failure(IllegalStateException("Ingen 'data' funnet ved henting av Oppfolgingsinfo fra veilarboppfolging"))

        val feil = oppfolgingsData.errors?.map {
            OppfolgingsinfoFeilInner(
                it.path?.joinToString(".") { it.toString() },
                it.message
            )
        }
        val erUnderOppfolging = oppfolgingsData.data?.oppfolging?.erUnderOppfolging
        val veileder = oppfolgingsData.data?.brukerStatus?.veilederTilordning?.veilederIdent
        val oppfolgingsenhet = oppfolgingsData.data?.oppfolgingsEnhet?.enhet

        if (oppfolgingsenhet == null  && (feil?.isEmpty() ?: true)) return Result.success(null)

        val oppfolgingsInfo = Oppfolgingsinfo(
            oppfolgingsEnhet = oppfolgingsenhet?.let { OppfolgingsinfoOppfolgingsEnhet(it.id, it.navn) },
            underOppfolging = erUnderOppfolging,
            primaerVeileder = veileder,
            feil = feil
        )

        return Result.success(oppfolgingsInfo)
    }
}