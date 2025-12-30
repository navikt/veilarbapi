package no.nav.poao.veilarbapi.aktivitet

import no.nav.poao.veilarbapi.dialog.InternDialog
import no.nav.poao.veilarbapi.dialog.mapDialog
import no.nav.veilarbaktivitet.model.SamtaleKanal
import no.nav.veilarbapi.model.Behandling
import no.nav.veilarbapi.model.Sokeavtale
import no.nav.veilarbapi.model.Samtalereferat
import no.nav.veilarbapi.model.StillingFraNav
import no.nav.veilarbapi.model.Egenaktivitet
import no.nav.veilarbapi.model.Mote
import no.nav.veilarbapi.model.Dialog
import no.nav.veilarbapi.model.AktivitetStatus
import no.nav.veilarbapi.model.Baseaktivitet
import no.nav.veilarbapi.model.StillingFraNavAllOfCvKanDelesData

typealias InternAktivitet = no.nav.veilarbaktivitet.model.Aktivitet
typealias InternSokeavtale = no.nav.veilarbaktivitet.model.Sokeavtale
typealias InternBehandling = no.nav.veilarbaktivitet.model.Behandling
typealias InternMote = no.nav.veilarbaktivitet.model.Mote
typealias InternSamtalereferat = no.nav.veilarbaktivitet.model.Samtalereferat
typealias InternStillingFraNav = no.nav.veilarbaktivitet.model.StillingFraNav
typealias InternEgenAktivitet = no.nav.veilarbaktivitet.model.Egenaktivitet
typealias Aktivitet = Baseaktivitet

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

private fun mapTilSokeavtale(aktivitet: InternSokeavtale, dialog: Dialog?): Baseaktivitet {
    return Sokeavtale(
        avtaltMedNav = aktivitet.avtaltMedNav,
        status = aktivitet.status?.name?.let { AktivitetStatus.valueOf(it) },
        beskrivelse = aktivitet.beskrivelse,
        tittel = aktivitet.tittel,
        fraDato = aktivitet.fraDato,
        tilDato = aktivitet.tilDato,
        opprettetDato = aktivitet.opprettetDato,
        endretDato = aktivitet.endretDato,
        dialog = dialog,
        aktivitetType = "Sokeavtale",
        aktivitetTypeNavn = "Søkeavtale",
        antallStillingerIUken = aktivitet.antallStillingerIUken,
        avtaleOppfolging = aktivitet.avtaleOppfolging
    )
}

private fun mapTilBehandling(aktivitet: InternBehandling, dialog: Dialog?): Aktivitet {
    return Behandling(
        avtaltMedNav = aktivitet.avtaltMedNav,
        status = aktivitet.status?.name?.let { AktivitetStatus.valueOf(it) },
        beskrivelse = aktivitet.beskrivelse,
        tittel = aktivitet.tittel,
        fraDato = aktivitet.fraDato,
        tilDato = aktivitet.tilDato,
        opprettetDato = aktivitet.opprettetDato,
        endretDato = aktivitet.endretDato,
        dialog = dialog,
        aktivitetType = "Behandling",
        aktivitetTypeNavn = "Medisinsk behandling",
        behandlingSted = aktivitet.behandlingSted,
    )
}

private fun mapTilMote(aktivitet: InternMote, dialog: Dialog?): Aktivitet {
    val moteform = when (aktivitet.kanal) {
        SamtaleKanal.OPPMOTE -> "Oppmøte"
        SamtaleKanal.TELEFON -> "Telefonmøte"
        SamtaleKanal.INTERNETT -> "Videomøte"
        else -> null
    }
    return Mote(
        avtaltMedNav = aktivitet.avtaltMedNav,
        status = aktivitet.status?.name?.let { AktivitetStatus.valueOf(it) },
        beskrivelse = aktivitet.beskrivelse,
        tittel = aktivitet.tittel,
        fraDato = aktivitet.fraDato,
        tilDato = aktivitet.tilDato,
        opprettetDato = aktivitet.opprettetDato,
        endretDato = aktivitet.endretDato,
        dialog = dialog,
        aktivitetType = "Mote",
        aktivitetTypeNavn = "Møte med NAV",
        adresse = aktivitet.adresse,
        forberedelser = aktivitet.forberedelser,
        kanal = moteform,
        referat = aktivitet.referat
    )
}

private fun mapTilSamtalereferat(aktivitet: InternSamtalereferat, dialog: Dialog?): Aktivitet {
    val moteform = when (aktivitet.kanal) {
        SamtaleKanal.OPPMOTE -> "Oppmøte"
        SamtaleKanal.TELEFON -> "Telefonmøte"
        SamtaleKanal.INTERNETT -> "Videomøte"
        else -> null
    }

    return Samtalereferat(
        avtaltMedNav = aktivitet.avtaltMedNav,
        status = aktivitet.status?.name?.let { AktivitetStatus.valueOf(it) },
        beskrivelse = aktivitet.beskrivelse,
        tittel = aktivitet.tittel,
        fraDato = aktivitet.fraDato,
        tilDato = aktivitet.tilDato,
        opprettetDato = aktivitet.opprettetDato,
        endretDato = aktivitet.endretDato,
        dialog = dialog,
        aktivitetType = "Samtalereferat",
        aktivitetTypeNavn = "Samtalereferat",
        kanal = moteform,
        referat = aktivitet.referat
    )
}

private fun mapTilStillingFraNav(aktivitet: InternStillingFraNav, dialog: Dialog?): Aktivitet {
    val sfnSoknadsstatus = when (aktivitet.soknadsstatus) {
        no.nav.veilarbaktivitet.model.StillingFraNav.Soknadsstatus.VENTER -> "Venter på å bli kontaktet av NAV eller arbeidsgiver"
        no.nav.veilarbaktivitet.model.StillingFraNav.Soknadsstatus.CV_DELT -> "CV er delt med arbeidsgiver"
        no.nav.veilarbaktivitet.model.StillingFraNav.Soknadsstatus.SKAL_PAA_INTERVJU -> "Skal på intervju"
        no.nav.veilarbaktivitet.model.StillingFraNav.Soknadsstatus.JOBBTILBUD -> "Fått jobbtilbud"
        no.nav.veilarbaktivitet.model.StillingFraNav.Soknadsstatus.AVSLAG -> "Ikke fått jobben"
        no.nav.veilarbaktivitet.model.StillingFraNav.Soknadsstatus.IKKE_FATT_JOBBEN -> "Ikke fått jobben"
        no.nav.veilarbaktivitet.model.StillingFraNav.Soknadsstatus.FATT_JOBBEN -> "Fått jobben"
        else -> null
    }

    val stillingFraNavCvKanDelesData = aktivitet.cvKanDelesData?.let { cvData ->
        StillingFraNavAllOfCvKanDelesData(
            kanDeles = cvData.kanDeles,
            endretTidspunkt = cvData.endretTidspunkt,
            endretAv = cvData.endretAv,
            endretAvType = cvData.endretAvType?.name?.let { StillingFraNavAllOfCvKanDelesData.EndretAvType.valueOf(it) },
            avtaltDato = cvData.avtaltDato
        )
    }

    return StillingFraNav(
        avtaltMedNav = aktivitet.avtaltMedNav,
        status = aktivitet.status?.name?.let { AktivitetStatus.valueOf(it) },
        beskrivelse = aktivitet.beskrivelse,
        tittel = aktivitet.tittel,
        fraDato = aktivitet.fraDato,
        tilDato = aktivitet.tilDato,
        opprettetDato = aktivitet.opprettetDato,
        endretDato = aktivitet.endretDato,
        dialog = dialog,
        aktivitetType = "StillingFraNav",
        aktivitetTypeNavn = "Stilling fra NAV",
        cvKanDelesData = stillingFraNavCvKanDelesData,
        soknadsfrist = aktivitet.soknadsfrist,
        svarfrist = aktivitet.svarfrist,
        arbeidsgiver = aktivitet.arbeidsgiver,
        bestillingsId = aktivitet.bestillingsId,
        stillingsId = aktivitet.stillingsId,
        arbeidssted = aktivitet.arbeidssted,
        soknadsstatus = sfnSoknadsstatus
    )
}

private fun mapTilEgenaktivitet(aktivitet: InternEgenAktivitet, dialog: Dialog?): Aktivitet {
    return Egenaktivitet(
        avtaltMedNav = aktivitet.avtaltMedNav,
        status = aktivitet.status?.name?.let { AktivitetStatus.valueOf(it) },
        beskrivelse = aktivitet.beskrivelse,
        tittel = aktivitet.tittel,
        fraDato = aktivitet.fraDato,
        tilDato = aktivitet.tilDato,
        opprettetDato = aktivitet.opprettetDato,
        endretDato = aktivitet.endretDato,
        dialog = dialog,
        aktivitetType = "Egenaktivitet",
        aktivitetTypeNavn = "Jobbrettet egenaktivitet",
        oppfolging = aktivitet.oppfolging,
        hensikt = aktivitet.hensikt
    )
}
