package no.nav.poao.util

import no.nav.poao.veilarbapi.dialog.InternDialog
import no.nav.veilarbdialog.model.Henvendelse
import org.threeten.bp.OffsetDateTime
import java.util.*
import kotlin.random.Random

class InternDialogBuilder {
    companion object {
        fun nyDialog(kvp: Boolean = false): InternDialog {
            val id = Random.nextLong().toString()

            val henvendelse = Henvendelse().apply {
                dialogId = id
                avsenderType = Henvendelse.AvsenderTypeEnum.BRUKER
                avsenderId = "42"
                sendtDato = OffsetDateTime.now()
                lestAvBruker = true
                lestAvVeileder = false
                tekst = "tekst"
            }

            val dialogHenvendelser = listOf(henvendelse)

            val internDialog = InternDialog().apply {
                dialogId = id
                aktivitetId = Random.nextLong().toString()
                oppfolgingsperiodeId = UUID.randomUUID()
                overskrift = "overskrift"
                venterSvarNav = true
                venterSvarBruker = false
                opprettetDato = OffsetDateTime.now()
                henvendelser = dialogHenvendelser
            }
            if (kvp) {
                return internDialog.kontorsperreEnhetId(Random.nextInt().toString())
            }
            return internDialog
        }
    }
}