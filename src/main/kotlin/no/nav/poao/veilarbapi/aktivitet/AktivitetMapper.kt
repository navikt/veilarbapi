package no.nav.poao.veilarbapi.aktivitet

import no.nav.poao.veilarbapi.dialog.InternDialog
import no.nav.poao.veilarbapi.dialog.mapDialog
import no.nav.veilarbapi.model.*

typealias InternAktivitet = no.nav.veilarbaktivitet.model.Aktivitet
typealias InternSokeavtale = no.nav.veilarbaktivitet.model.Sokeavtale
typealias InternBehandling = no.nav.veilarbaktivitet.model.Behandling
typealias InternMote = no.nav.veilarbaktivitet.model.Mote
typealias InternSamtalereferat = no.nav.veilarbaktivitet.model.Samtalereferat
typealias InternStillingFraNav = no.nav.veilarbaktivitet.model.StillingFraNav
typealias InternEgenAktivitet = no.nav.veilarbaktivitet.model.Egenaktivitet

private val FILTER_AKTIVITETSTYPER = listOf("jobbsoeking", "ijobb")

fun mapAktiviteter(aktiviteter: List<InternAktivitet>?, dialoger: List<InternDialog> = emptyList()): List<Aktivitet> {
    if (aktiviteter == null) return emptyList()
    return aktiviteter
        .filter { it.aktivitetType !in FILTER_AKTIVITETSTYPER }
        .map { aktivitet -> mapAktivitet(aktivitet, dialoger.firstOrNull { it.aktivitetId == aktivitet.aktivitetId }) }
}

fun mapAktivitet(aktivitet: InternAktivitet, dialog: InternDialog? = null): Aktivitet {
    val mappedDialog = dialog?.let { mapDialog(it) }

    return when (aktivitet.aktivitetType) {
        "sokeavtale" -> mapTilSokeavtale(aktivitet as InternSokeavtale, mappedDialog)
        "behandling" -> mapTilBehandling(aktivitet as InternBehandling, mappedDialog)
        "mote" -> mapTilMote(aktivitet as InternMote, mappedDialog)
        "samtalereferat" -> mapTilSamtalereferat(aktivitet as InternSamtalereferat, mappedDialog)
        "stilling_fra_nav" -> mapTilStillingFraNav(aktivitet as InternStillingFraNav, mappedDialog)
        "egenaktivitet" -> mapTilEgenaktivitet(aktivitet as InternEgenAktivitet, mappedDialog)
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

private fun mapTilSokeavtale(aktivitet: InternSokeavtale, dialog: Dialog?): Aktivitet {
    val sokeavtale = Sokeavtale().apply {
        aktivitetType = "Sokeavtale"
        aktivitetTypeNavn = "Søkeavtale"
        antallStillingerIUken = aktivitet.antallStillingerIUken
        avtaleOppfolging = aktivitet.avtaleOppfolging
    }

    merge(aktivitet, sokeavtale, dialog)

    return Aktivitet(sokeavtale)
}


private fun mapTilBehandling(aktivitet: InternBehandling, dialog: Dialog?): Aktivitet {
    val behandling = Behandling().apply {
        aktivitetType = "Behandling"
        aktivitetTypeNavn = "Medisinsk behandling"
        behandlingSted = aktivitet.behandlingSted
    }

    merge(aktivitet, behandling, dialog)

    return Aktivitet(behandling)
}

private fun mapTilMote(aktivitet: InternMote, dialog: Dialog?): Aktivitet {
    val moteform = when (aktivitet.kanal) {
        no.nav.veilarbaktivitet.model.Mote.KanalEnum.OPPMOTE -> "Oppmøte"
        no.nav.veilarbaktivitet.model.Mote.KanalEnum.TELEFON -> "Telefonmøte"
        no.nav.veilarbaktivitet.model.Mote.KanalEnum.INTERNETT -> "Videomøte"
        else -> null
    }

    val mote = Mote().apply {
        aktivitetType = "Mote"
        aktivitetTypeNavn = "Møte med NAV"
        adresse = aktivitet.adresse
        forberedelser = aktivitet.forberedelser
        kanal = moteform
        referat = aktivitet.referat
    }

    merge(aktivitet, mote, dialog)

    return Aktivitet(mote)
}

private fun mapTilSamtalereferat(aktivitet: InternSamtalereferat, dialog: Dialog?): Aktivitet {
    val moteform = when (aktivitet.kanal) {
        no.nav.veilarbaktivitet.model.Samtalereferat.KanalEnum.OPPMOTE -> "Oppmøte"
        no.nav.veilarbaktivitet.model.Samtalereferat.KanalEnum.TELEFON -> "Telefonmøte"
        no.nav.veilarbaktivitet.model.Samtalereferat.KanalEnum.INTERNETT -> "Videomøte"
        else -> null
    }

    val samtalereferat = Samtalereferat().apply {
        aktivitetType = "Samtalereferat"
        aktivitetTypeNavn = "Samtalereferat"
        kanal = moteform
        referat = aktivitet.referat
    }

    merge(aktivitet, samtalereferat, dialog)

    return Aktivitet(samtalereferat)
}

private fun mapTilStillingFraNav(aktivitet: InternStillingFraNav, dialog: Dialog?): Aktivitet {
    val sfnSoknadsstatus = when (aktivitet.soknadsstatus) {
        no.nav.veilarbaktivitet.model.StillingFraNav.SoknadsstatusEnum.VENTER -> "Venter på å bli kontaktet av NAV eller arbeidsgiver"
        no.nav.veilarbaktivitet.model.StillingFraNav.SoknadsstatusEnum.CV_DELT -> "CV er delt med arbeidsgiver"
        no.nav.veilarbaktivitet.model.StillingFraNav.SoknadsstatusEnum.SKAL_PAA_INTERVJU -> "Skal på intervju"
        no.nav.veilarbaktivitet.model.StillingFraNav.SoknadsstatusEnum.JOBBTILBUD -> "Fått jobbtilbud"
        no.nav.veilarbaktivitet.model.StillingFraNav.SoknadsstatusEnum.AVSLAG -> "Ikke fått jobben"
        no.nav.veilarbaktivitet.model.StillingFraNav.SoknadsstatusEnum.IKKE_FATT_JOBBEN -> "Ikke fått jobben"
        no.nav.veilarbaktivitet.model.StillingFraNav.SoknadsstatusEnum.FATT_JOBBEN -> "Fått jobben"
        else -> null
    }

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
        aktivitetType = "StillingFraNav"
        aktivitetTypeNavn = "Stilling fra NAV"
        cvKanDelesData = stillingFraNavCvKanDelesData
        soknadsfrist = aktivitet.soknadsfrist
        svarfrist = aktivitet.svarfrist
        arbeidsgiver = aktivitet.arbeidsgiver
        bestillingsId = aktivitet.bestillingsId
        stillingsId = aktivitet.stillingsId
        arbeidssted = aktivitet.arbeidssted
        soknadsstatus = sfnSoknadsstatus
    }

    merge(aktivitet, stillingFraNav, dialog)

    return Aktivitet(stillingFraNav)
}

private fun mapTilEgenaktivitet(aktivitet: InternEgenAktivitet, dialog: Dialog?): Aktivitet {
    val egenAktivitet = Egenaktivitet().apply {
        aktivitetType = "Egenaktivitet"
        aktivitetTypeNavn = "Jobbrettet egenaktivitet"
        oppfolging = aktivitet.oppfolging
        hensikt = aktivitet.hensikt
    }

    merge(aktivitet, egenAktivitet, dialog)

    return Aktivitet(egenAktivitet)
}
