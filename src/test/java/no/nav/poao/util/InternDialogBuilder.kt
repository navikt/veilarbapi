package no.nav.poao.util

import no.nav.poao.veilarbapi.dialog.InternDialog
import no.nav.veilarbdialog.model.Henvendelse
import org.threeten.bp.OffsetDateTime
import java.util.*
import kotlin.random.Random

class InternDialogBuilder {
    companion object {
        fun nyDialog(kvpDialog: Boolean = false, kvpHenvendelse: Boolean = false): InternDialog {
            val henvendelse = Henvendelse().apply {
                avsenderType = Henvendelse.AvsenderTypeEnum.BRUKER
                avsenderId = "42"
                sendtDato = OffsetDateTime.now()
                lestAvBruker = true
                lestAvVeileder = false
                tekst = "tekst"
            }

            if (kvpHenvendelse) {
                henvendelse.kontorsperreEnhetId(Random.nextInt().toString())
            }

            val dialogHenvendelser = listOf(henvendelse)

            val dialog = InternDialog().apply {
                aktivitetId = Random.nextLong().toString()
                oppfolgingsperiodeId = UUID.randomUUID()
                overskrift = "overskrift"
                venterSvarNav = true
                venterSvarBruker = false
                opprettetDato = OffsetDateTime.now()
                henvendelser = dialogHenvendelser
            }
            if (kvpDialog) {
                dialog.kontorsperreEnhetId(Random.nextInt().toString())
            }
            return dialog
        }

        fun nyHenvendelsePaaDialog(dialog: InternDialog, kvpHenvendelse: Boolean = false) {
            val henvendelse = Henvendelse().apply {
                dialogId = dialog.dialogId
                avsenderType = Henvendelse.AvsenderTypeEnum.BRUKER
                avsenderId = "42"
                sendtDato = OffsetDateTime.now()
                lestAvBruker = true
                lestAvVeileder = false
                tekst = "tekst"
            }

            if (kvpHenvendelse) {
                henvendelse.kontorsperreEnhetId(Random.nextInt().toString())
            }

            val nyHenvendelse = listOf(henvendelse)
            dialog.apply {
                henvendelser = dialog.henvendelser?.plus(nyHenvendelse)
            }
        }
    }
}