package no.nav.poao.util

import no.nav.poao.veilarbapi.aktivitet.InternAktivitet
import no.nav.veilarbaktivitet.model.*
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.*
import kotlin.random.Random

class InternAktivitetBuilder {
    companion object {
        fun nyAktivitet(type: String, kvp: Boolean = false): InternAktivitet {
            val aktivitet = when (type) {
                "egenaktivitet" -> nyEgenaktivitet()
                "jobbsoeking" -> nyJobbsoeking()
                "sokeavtale" -> nySokeavtale()
                "ijobb" -> nyIjobb()
                "behandling" -> nyBehandling()
                "mote" -> nyMote()
                "samtalereferat" -> nySamtalereferat()
                "stilling_fra_nav" -> nyStillingFraNav()
                else -> throw IllegalArgumentException("Ukjent aktivitetstype")
            }

            val aktivitetMedFellesFelter = nyAktivitet(aktivitet)

            if (kvp) {
                val enhet = Random.nextInt().toString()
                return when (aktivitetMedFellesFelter) {
                    is Egenaktivitet -> aktivitetMedFellesFelter.copy(kontorsperreEnhetId = enhet)
                    is Jobbsoeking -> aktivitetMedFellesFelter.copy(kontorsperreEnhetId = enhet)
                    is Sokeavtale -> aktivitetMedFellesFelter.copy(kontorsperreEnhetId = enhet)
                    is Ijobb -> aktivitetMedFellesFelter.copy(kontorsperreEnhetId = enhet)
                    is Behandling -> aktivitetMedFellesFelter.copy(kontorsperreEnhetId = enhet)
                    is Mote -> aktivitetMedFellesFelter.copy(kontorsperreEnhetId = enhet)
                    is Samtalereferat -> aktivitetMedFellesFelter.copy(kontorsperreEnhetId = enhet)
                    is StillingFraNav -> aktivitetMedFellesFelter.copy(kontorsperreEnhetId = enhet)
                    else -> aktivitetMedFellesFelter
                }
            }
            return aktivitetMedFellesFelter
        }

        private fun nyAktivitet(aktivitet: InternAktivitet): InternAktivitet {
            // The model types are now Kotlin data classes, so use copy(...) to create
            // a new instance with the common fields populated instead of mutating via apply.
            val nowOffset = OffsetDateTime.now()
            val commonAktivitetId = Random.nextLong().toString()
            val commonOppfolgingsperiodeId = UUID.randomUUID()

            return when (aktivitet) {
                is Egenaktivitet -> aktivitet.copy(
                    aktivitetId = commonAktivitetId,
                    oppfolgingsperiodeId = commonOppfolgingsperiodeId,
                    avtaltMedNav = false,
                    status = AktivitetStatus.PLANLAGT,
                    beskrivelse = "beskrivelse",
                    tittel = "tittel",
                    fraDato = nowOffset,
                    tilDato = nowOffset,
                    opprettetDato = nowOffset,
                    endretDato = nowOffset
                )
                is Jobbsoeking -> aktivitet.copy(
                    aktivitetId = commonAktivitetId,
                    oppfolgingsperiodeId = commonOppfolgingsperiodeId,
                    avtaltMedNav = false,
                    status = AktivitetStatus.PLANLAGT,
                    beskrivelse = "beskrivelse",
                    tittel = "tittel",
                    fraDato = nowOffset,
                    tilDato = nowOffset,
                    opprettetDato = nowOffset,
                    endretDato = nowOffset
                )
                is Sokeavtale -> aktivitet.copy(
                    aktivitetId = commonAktivitetId,
                    oppfolgingsperiodeId = commonOppfolgingsperiodeId,
                    avtaltMedNav = false,
                    status = AktivitetStatus.PLANLAGT,
                    beskrivelse = "beskrivelse",
                    tittel = "tittel",
                    fraDato = nowOffset,
                    tilDato = nowOffset,
                    opprettetDato = nowOffset,
                    endretDato = nowOffset
                )
                is Ijobb -> aktivitet.copy(
                    aktivitetId = commonAktivitetId,
                    oppfolgingsperiodeId = commonOppfolgingsperiodeId,
                    avtaltMedNav = false,
                    status = AktivitetStatus.PLANLAGT,
                    beskrivelse = "beskrivelse",
                    tittel = "tittel",
                    fraDato = nowOffset,
                    tilDato = nowOffset,
                    opprettetDato = nowOffset,
                    endretDato = nowOffset
                )
                is Behandling -> aktivitet.copy(
                    aktivitetId = commonAktivitetId,
                    oppfolgingsperiodeId = commonOppfolgingsperiodeId,
                    avtaltMedNav = false,
                    status = AktivitetStatus.PLANLAGT,
                    beskrivelse = "beskrivelse",
                    tittel = "tittel",
                    fraDato = nowOffset,
                    tilDato = nowOffset,
                    opprettetDato = nowOffset,
                    endretDato = nowOffset
                )
                is Mote -> aktivitet.copy(
                    aktivitetId = commonAktivitetId,
                    oppfolgingsperiodeId = commonOppfolgingsperiodeId,
                    avtaltMedNav = false,
                    status = AktivitetStatus.PLANLAGT,
                    beskrivelse = "beskrivelse",
                    tittel = "tittel",
                    fraDato = nowOffset,
                    tilDato = nowOffset,
                    opprettetDato = nowOffset,
                    endretDato = nowOffset
                )
                is Samtalereferat -> aktivitet.copy(
                    aktivitetId = commonAktivitetId,
                    oppfolgingsperiodeId = commonOppfolgingsperiodeId,
                    avtaltMedNav = false,
                    status = AktivitetStatus.PLANLAGT,
                    beskrivelse = "beskrivelse",
                    tittel = "tittel",
                    fraDato = nowOffset,
                    tilDato = nowOffset,
                    opprettetDato = nowOffset,
                    endretDato = nowOffset
                )
                is StillingFraNav -> aktivitet.copy(
                    aktivitetId = commonAktivitetId,
                    oppfolgingsperiodeId = commonOppfolgingsperiodeId,
                    avtaltMedNav = false,
                    status = AktivitetStatus.PLANLAGT,
                    beskrivelse = "beskrivelse",
                    tittel = "tittel",
                    fraDato = nowOffset,
                    tilDato = nowOffset,
                    opprettetDato = nowOffset,
                    endretDato = nowOffset
                )
                else -> aktivitet
            }
        }

        fun nyEgenaktivitet(): Egenaktivitet {
            // Use primary constructor with named params for Kotlin data classes
            val egenaktivitet = Egenaktivitet(
                aktivitetType = "egenaktivitet",
                hensikt = "hensikt",
                oppfolging = "oppfolging"
            )

            return nyAktivitet(egenaktivitet) as Egenaktivitet
        }

        fun nyJobbsoeking(): Jobbsoeking {
            val jobbsoeking = Jobbsoeking(
                aktivitetType = "jobbsoeking",
                arbeidsgiver = "arbeidsgiver",
                stillingsTittel = "stillingstittel",
                arbeidssted = "arbeidssted",
                stillingsoekEtikett = Jobbsoeking.StillingsoekEtikett.SOKNAD_SENDT
            )

            return nyAktivitet(jobbsoeking) as Jobbsoeking
        }

        fun nySokeavtale(): Sokeavtale {
            val sokeavtale = Sokeavtale(
                aktivitetType = "sokeavtale",
                antallStillingerSokes = 10,
                antallStillingerIUken = 2,
                avtaleOppfolging = "oppfolging"
            )

            return nyAktivitet(sokeavtale) as Sokeavtale
        }

        fun nyIjobb(): Ijobb {
            val ijobb = Ijobb(
                aktivitetType = "ijobb",
                jobbStatusType = Ijobb.JobbStatusType.DELTID,
                ansettelsesforhold = "ansettelsesforhold",
                arbeidstid = "arbeidstid"
            )

            return nyAktivitet(ijobb) as Ijobb
        }

        fun nyBehandling(): Behandling {
            val behandling = Behandling(
                aktivitetType = "behandling",
                behandlingType = "behandlingstype",
                behandlingSted = "behandlingssted",
                effekt = "effekt",
                behandlingOppfolging = "behandlingoppfolging"
            )

            return nyAktivitet(behandling) as Behandling
        }

        fun nyMote(): Mote {
            val mote = Mote(
                aktivitetType = "mote",
                adresse = "adresse",
                forberedelser = "forberedelser",
                kanal = SamtaleKanal.OPPMOTE,
                referat = "referat",
                referatPublisert = true
            )

            return nyAktivitet(mote) as Mote
        }

        fun nySamtalereferat(): Samtalereferat {
            val samtalereferat = Samtalereferat(
                aktivitetType = "samtalereferat",
                kanal = SamtaleKanal.OPPMOTE,
                referat = "referat",
                referatPublisert = true
            )

            return nyAktivitet(samtalereferat) as Samtalereferat
        }

        fun nyStillingFraNav(): StillingFraNav {
            val stillingFraNavCvKanDelesData = StillingFraNavAllOfCvKanDelesData(
                kanDeles = true,
                endretTidspunkt = OffsetDateTime.now(),
                endretAv = "endret av",
                endretAvType = StillingFraNavAllOfCvKanDelesData.EndretAvType.BRUKER,
                avtaltDato = LocalDate.now()
            )

            val stillingFraNav = StillingFraNav(
                aktivitetType = "stilling_fra_nav",
                cvKanDelesData = stillingFraNavCvKanDelesData,
                soknadsfrist = "soknadsfrist",
                svarfrist = LocalDate.now(),
                arbeidsgiver = "arbeidsgiver",
                bestillingsId = "bestillingsid",
                stillingsId = "stillingsid",
                arbeidssted = "arbeidssted",
                soknadsstatus = StillingFraNav.Soknadsstatus.VENTER
            )

            return nyAktivitet(stillingFraNav) as StillingFraNav
        }

    }
}