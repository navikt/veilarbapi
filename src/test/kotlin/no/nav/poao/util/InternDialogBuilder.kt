package no.nav.poao.util

import no.nav.poao.veilarbapi.dialog.InternDialog
import no.nav.veilarbdialog.model.AvsenderType
import no.nav.veilarbdialog.model.Dialog
import no.nav.veilarbdialog.model.Henvendelse
import java.time.OffsetDateTime
import java.util.*
import kotlin.random.Random

class InternDialogBuilder {
    companion object {
        fun nyDialog(kvpDialog: Boolean = false, kvpHenvendelse: Boolean = false): InternDialog {
            val henvendelse = Henvendelse(
                avsenderType = AvsenderType.BRUKER,
                avsenderId = "42",
                sendtDato = OffsetDateTime.now(),
                lestAvBruker = true,
                lestAvVeileder = false,
                tekst = "tekst",
                kontorsperreEnhetId = if (kvpHenvendelse) Random.nextInt().toString() else null
            )

            return InternDialog(
                aktivitetId = Random.nextLong().toString(),
                oppfolgingsperiodeId = UUID.randomUUID(),
                overskrift = "overskrift",
                venterSvarNav = true,
                venterSvarBruker = false,
                opprettetDato = OffsetDateTime.now(),
                henvendelser = listOf(henvendelse),
                kontorsperreEnhetId = if (kvpDialog) Random.nextInt().toString() else null
            )
        }
    }
}

fun InternDialog.nyHenvendelsePaaDialog(kvpHenvendelse: Boolean = false): InternDialog {
    val henvendelse = Henvendelse(
        dialogId = this.dialogId,
        avsenderType = AvsenderType.BRUKER,
        avsenderId = "42",
        sendtDato = OffsetDateTime.now(),
        lestAvBruker = true,
        lestAvVeileder = false,
        tekst = "tekst",
        kontorsperreEnhetId = if (kvpHenvendelse) Random.nextInt().toString() else null
    )

    val nyHenvendelse = listOf(henvendelse)
    return this.copy(
        henvendelser = this.henvendelser?.plus(nyHenvendelse)
    )
}