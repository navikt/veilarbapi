package no.nav.poao.veilarbapi.aktivitet

import no.nav.poao.veilarbapi.dialog.InternDialog
import no.nav.poao.veilarbapi.dialog.mapDialog
import no.nav.veilarbapi.model.*

typealias InternAktivitet = no.nav.veilarbaktivitet.model.Aktivitet
typealias InternEgenaktivitet = no.nav.veilarbaktivitet.model.Egenaktivitet
typealias InternJobbsoeking = no.nav.veilarbaktivitet.model.Jobbsoeking
typealias InternSokeavtale = no.nav.veilarbaktivitet.model.Sokeavtale
typealias InternIjobb = no.nav.veilarbaktivitet.model.Ijobb
typealias InternBehandling = no.nav.veilarbaktivitet.model.Behandling
typealias InternMote = no.nav.veilarbaktivitet.model.Mote
typealias InternSamtalereferat = no.nav.veilarbaktivitet.model.Samtalereferat
typealias InternStillingFraNav = no.nav.veilarbaktivitet.model.StillingFraNav


fun mapAktiviteter(
    aktiviteter: List<InternAktivitet>?,
    dialoger: List<InternDialog>? = null,
    filtrerKvp: Boolean = true
): List<Aktivitet>? {
    if (filtrerKvp) {
        val filtrerteAktiviteter = aktiviteter?.filter { it.kontorsperreEnhetId == null }
        val filtrerteDialoger = dialoger?.filter { it.kontorsperreEnhetId == null }

        return mapAktiviteter(filtrerteAktiviteter, filtrerteDialoger)
    }

    return mapAktiviteter(aktiviteter, dialoger)
}

private fun mapAktiviteter(aktiviteter: List<InternAktivitet>?, dialoger: List<InternDialog>? = null,): List<Aktivitet>? {
    return aktiviteter?.map { a ->
        mapAktivitet(a, dialoger?.find { d -> d.aktivitetId == a.aktivitetId })
    }
}

private fun mapAktivitet(aktivitet: InternAktivitet, dialog: InternDialog? = null): Aktivitet {
    val mappedDialog = dialog?.let { mapDialog(it) }

    return when (aktivitet.aktivitetType) {
        "egenaktivitet" -> mapTilEgenaktivitet(aktivitet as InternEgenaktivitet, mappedDialog)
        "jobbsoeking" -> mapTilJobbsoeking(aktivitet as InternJobbsoeking, mappedDialog)
        "sokeavtale" -> mapTilSokeavtale(aktivitet as InternSokeavtale, mappedDialog)
        "ijobb" -> mapTilIjobb(aktivitet as InternIjobb, mappedDialog)
        "behandling" -> mapTilBehandling(aktivitet as InternBehandling, mappedDialog)
        "mote" -> mapTilMote(aktivitet as InternMote, mappedDialog)
        "samtalereferat" -> mapTilSamtalereferat(aktivitet as InternSamtalereferat, mappedDialog)
        "stilling_fra_nav" -> mapTilStillingFraNav(aktivitet as InternStillingFraNav, mappedDialog)
        else -> throw IllegalArgumentException("Ukjent aktivitetstype")
    }
}

private fun merge(gammelAktivitet: InternAktivitet, nyAktivitet: Baseaktivitet, maybeDialog: Dialog?) {
    nyAktivitet.apply {
        avtaltMedNav = gammelAktivitet.avtaltMedNav
        status = gammelAktivitet.status?.name?.let { Baseaktivitet.StatusEnum.valueOf(it) }
        beskrivelse = gammelAktivitet.beskrivelse
        tittel = gammelAktivitet.tittel
        fraDato = gammelAktivitet.fraDato
        tilDato = gammelAktivitet.tilDato
        opprettetDato = gammelAktivitet.opprettetDato
        endretDato = gammelAktivitet.endretDato
        dialog = maybeDialog
    }
}

private fun mapTilEgenaktivitet(aktivitet: InternEgenaktivitet, dialog: Dialog?): Aktivitet {
    val egenaktivitet = Egenaktivitet().apply {
        aktivitetType = "egenaktivitet"
        hensikt = aktivitet.hensikt
        oppfolging = aktivitet.oppfolging
    }

    merge(aktivitet, egenaktivitet, dialog)

    return Aktivitet(egenaktivitet)
}

private fun mapTilJobbsoeking(aktivitet: InternJobbsoeking, dialog: Dialog?): Aktivitet {
    val jobbsoeking = Jobbsoeking().apply {
        aktivitetType = "jobbsoeking"
        arbeidsgiver = aktivitet.arbeidsgiver
        stillingsTittel = aktivitet.stillingsTittel
        arbeidssted = aktivitet.arbeidssted
        stillingsoekEtikett = aktivitet.stillingsoekEtikett?.name?.let { Jobbsoeking.StillingsoekEtikettEnum.valueOf(it) }
        kontaktPerson = aktivitet.kontaktPerson
    }

    merge(aktivitet, jobbsoeking, dialog)

    return Aktivitet(jobbsoeking)
}

private fun mapTilSokeavtale(aktivitet: InternSokeavtale, dialog: Dialog?): Aktivitet {
    val sokeavtale = Sokeavtale().apply {
        aktivitetType = "sokeavtale"
        antallStillingerIUken = aktivitet.antallStillingerIUken
        avtaleOppfolging = aktivitet.avtaleOppfolging
    }

    merge(aktivitet, sokeavtale, dialog)

    return Aktivitet(sokeavtale)
}

private fun mapTilIjobb(aktivitet: InternIjobb, dialog: Dialog?): Aktivitet {
    val ijobb = Ijobb().apply {
        aktivitetType = "ijobb"
        jobbStatusType = aktivitet.jobbStatusType?.name?.let { Ijobb.JobbStatusTypeEnum.valueOf(it) }
        ansettelsesforhold = aktivitet.ansettelsesforhold
        arbeidstid = aktivitet.arbeidstid
    }

    merge(aktivitet, ijobb, dialog)

    return Aktivitet(ijobb)
}

private fun mapTilBehandling(aktivitet: InternBehandling, dialog: Dialog?): Aktivitet {
    val behandling = Behandling().apply {
        aktivitetType = "behandling"
        behandlingSted = aktivitet.behandlingSted
    }

    merge(aktivitet, behandling, dialog)

    return Aktivitet(behandling)
}

private fun mapTilMote(aktivitet: InternMote, dialog: Dialog?): Aktivitet {
    val mote = Mote().apply {
        aktivitetType = "mote"
        adresse = aktivitet.adresse
        forberedelser = aktivitet.forberedelser
        kanal = aktivitet.kanal?.name?.let { Mote.KanalEnum.valueOf(it) }
        referat = aktivitet.referat
    }

    merge(aktivitet, mote, dialog)

    return Aktivitet(mote)
}

private fun mapTilSamtalereferat(aktivitet: InternSamtalereferat, dialog: Dialog?): Aktivitet {
    val samtalereferat = Samtalereferat().apply {
        aktivitetType = "samtalereferat"
        kanal = aktivitet.kanal?.name?.let { Samtalereferat.KanalEnum.valueOf(it) }
        referat = aktivitet.referat
    }

    merge(aktivitet, samtalereferat, dialog)

    return Aktivitet(samtalereferat)
}

private fun mapTilStillingFraNav(aktivitet: InternStillingFraNav, dialog: Dialog?): Aktivitet {
    val stillingFraNavCvKanDelesData: StillingFraNavAllOfCvKanDelesData? = aktivitet.cvKanDelesData?.let {
        StillingFraNavAllOfCvKanDelesData().apply {
            kanDeles = it.kanDeles
            endretTidspunkt = it.endretTidspunkt
            endretAv = it.endretAv
            endretAvType = it.endretAvType?.name?.let { StillingFraNavAllOfCvKanDelesData.EndretAvTypeEnum.valueOf(it) }
            avtaltDato = it.avtaltDato
        }
    }

    val stillingFraNav = StillingFraNav().apply {
        aktivitetType = "stillingFraNav"
        cvKanDelesData = stillingFraNavCvKanDelesData
        soknadsfrist = aktivitet.soknadsfrist
        svarfrist = aktivitet.svarfrist
        arbeidsgiver = aktivitet.arbeidsgiver
        bestillingsId = aktivitet.bestillingsId
        stillingsId = aktivitet.stillingsId
        arbeidssted = aktivitet.arbeidssted
        soknadsstatus = aktivitet.soknadsstatus?.name?.let { StillingFraNav.SoknadsstatusEnum.valueOf(it) }
    }

    merge(aktivitet, stillingFraNav, dialog)

    return Aktivitet(stillingFraNav)
}
