package no.nav.poao.util

import no.nav.poao.veilarbapi.aktivitet.InternAktivitet
import no.nav.veilarbaktivitet.model.*
import org.threeten.bp.LocalDate
import org.threeten.bp.OffsetDateTime
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
            if (kvp) {
                return aktivitet.kontorsperreEnhetId(Random.nextInt().toString())
            }
            return aktivitet
        }

        private fun nyAktivitet(aktivitet: InternAktivitet): InternAktivitet {
            return aktivitet.apply {
                aktivitetId = Random.nextLong().toString()
                oppfolgingsperiodeId = UUID.randomUUID()
                avtaltMedNav = false
                status = AktivitetStatus.PLANLAGT
                beskrivelse = "beskrivelse"
                tittel = "tittel"
                fraDato = OffsetDateTime.now()
                tilDato = OffsetDateTime.now()
                opprettetDato = OffsetDateTime.now()
                endretDato = OffsetDateTime.now()
            }
        }

        private fun nyEgenaktivitet(): InternAktivitet {
            val egenaktivitet = Egenaktivitet().apply {
                aktivitetType = "egenaktivitet"
                hensikt = "hensikt"
                oppfolging = "oppfolging"
            }

            return nyAktivitet(egenaktivitet)
        }

        private fun nyJobbsoeking(): InternAktivitet {
            val jobbsoeking = Jobbsoeking().apply {
                aktivitetType = "jobbsoeking"
                arbeidsgiver = "arbeidsgiver"
                stillingsTittel = "stillingstittel"
                arbeidssted = "arbeidssted"
                stillingsoekEtikett = Jobbsoeking.StillingsoekEtikettEnum.SOKNAD_SENDT
            }

            return nyAktivitet(jobbsoeking)
        }

        private fun nySokeavtale(): InternAktivitet {
            val sokeavtale = Sokeavtale().apply {
                aktivitetType = "sokeavtale"
                antallStillingerSokes = 10
                antallStillingerIUken = 2
                avtaleOppfolging = "oppfolging"
            }

            return nyAktivitet(sokeavtale)
        }

        private fun nyIjobb(): InternAktivitet {
            val ijobb = Ijobb().apply {
                aktivitetType = "ijobb"
                jobbStatusType = Ijobb.JobbStatusTypeEnum.DELTID
                ansettelsesforhold = "ansettelsesforhold"
                arbeidstid = "arbeidstid"
            }

            return nyAktivitet(ijobb)
        }

        private fun nyBehandling(): InternAktivitet {
            val behandling = Behandling().apply {
                aktivitetType = "behandling"
                behandlingType = "behandlingstype"
                behandlingSted = "behandlingssted"
                effekt = "effekt"
                behandlingOppfolging = "behandlingoppfolging"
            }

            return nyAktivitet(behandling)
        }

        private fun nyMote(): InternAktivitet {
            val mote = Mote().apply {
                aktivitetType = "mote"
                adresse = "adresse"
                forberedelser = "forberedelser"
                kanal = Mote.KanalEnum.OPPMOTE
                referat = "referat"
                referatPublisert = true
            }

            return nyAktivitet(mote)
        }

        private fun nySamtalereferat(): InternAktivitet {
            val samtalereferat = Samtalereferat().apply {
                aktivitetType = "samtalereferat"
                kanal = Samtalereferat.KanalEnum.OPPMOTE
                referat = "referat"
                referatPublisert = true
            }

            return nyAktivitet(samtalereferat)
        }

        private fun nyStillingFraNav(): InternAktivitet {
            val stillingFraNavCvKanDelesData = StillingFraNavAllOfCvKanDelesData().apply {
                kanDeles = true
                endretTidspunkt = OffsetDateTime.now()
                endretAv = "endret av"
                endretAvType = StillingFraNavAllOfCvKanDelesData.EndretAvTypeEnum.BRUKER
                avtaltDato = LocalDate.now()
            }

            val stillingFraNav = StillingFraNav().apply {
                aktivitetType = "stilling_fra_nav"
                cvKanDelesData = stillingFraNavCvKanDelesData
                soknadsfrist = "soknadsfrist"
                svarfrist = LocalDate.now()
                arbeidsgiver = "arbeidsgiver"
                bestillingsId = "bestillingsid"
                stillingsId = "stillingsid"
                arbeidssted = "arbeidssted"
                soknadsstatus = StillingFraNav.SoknadsstatusEnum.VENTER
            }

            return nyAktivitet(stillingFraNav)
        }

    }
}