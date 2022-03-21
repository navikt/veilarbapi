package no.nav.poao.mapper

import OppfolgingsperiodeDTO
import no.nav.poao.util.InternAktivitetBuilder
import no.nav.poao.util.InternDialogBuilder
import no.nav.poao.veilarbapi.aktivitet.mapAktivitet
import no.nav.poao.veilarbapi.aktivitet.mapAktiviteter
import no.nav.poao.veilarbapi.dialog.mapDialog
import no.nav.poao.veilarbapi.dialog.mapDialoger
import no.nav.poao.veilarbapi.oppfolging.mapOppfolgingsperioder
import no.nav.veilarbapi.model.Egenaktivitet
import no.nav.veilarbapi.model.Jobbsoeking
import no.nav.veilarbapi.model.Oppfolgingsperioder
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions
import org.threeten.bp.OffsetDateTime
import java.util.*
import kotlin.test.Test

class MapperTest {

    @Test
    fun testDialogMapper() {
        val internDialog = InternDialogBuilder.nyDialog()
        val dialog = mapDialog(internDialog)

        assertThat(dialog.tittel).isEqualTo(internDialog.overskrift)

        val internDialoger = listOf(
                internDialog,
                InternDialogBuilder.nyDialog(),
                InternDialogBuilder.nyDialog()
        )
        val dialoger = mapDialoger(internDialoger)

        assertThat(dialoger).hasSize(3)
    }

    @Test
    fun testAktivitetMapper() {
        val internAktivitet = InternAktivitetBuilder.nyAktivitet("egenaktivitet")
        val aktivitet = mapAktivitet(internAktivitet)

        assertThat(aktivitet.actualInstance).isInstanceOf(Egenaktivitet::class.java)

        val internAktiviteter = listOf(
            internAktivitet,
            InternAktivitetBuilder.nyAktivitet("jobbsoeking").aktivitetId("2"),
            InternAktivitetBuilder.nyAktivitet("sokeavtale").aktivitetId("6"),
            InternAktivitetBuilder.nyAktivitet("ijobb"),
            InternAktivitetBuilder.nyAktivitet("behandling"),
            InternAktivitetBuilder.nyAktivitet("mote"),
            InternAktivitetBuilder.nyAktivitet("samtalereferat"),
            InternAktivitetBuilder.nyAktivitet("stilling_fra_nav")
        )
        val internDialoger = listOf(
            InternDialogBuilder.nyDialog().aktivitetId("2").overskrift("test"),
            InternDialogBuilder.nyDialog().aktivitetId("6")
        )

        val aktiviteter = mapAktiviteter(internAktiviteter)

        assertThat(aktiviteter!!).hasSize(internAktiviteter.size)
        assertThat(aktiviteter[1].actualInstance).isInstanceOf(Jobbsoeking::class.java)
        assertThat(aktiviteter[1].jobbsoeking.dialog).isNull()

        val aktiviteter2 = mapAktiviteter(internAktiviteter, internDialoger)

        assertThat(aktiviteter2!![1].jobbsoeking.dialog).isNotNull
        assertThat(aktiviteter2[1].jobbsoeking.dialog?.tittel).isEqualTo("test")
    }

    @Test
    fun testOppfolgingsperiodeMapper() {
        val uuid1 = UUID.randomUUID()
        val uuid2 = UUID.randomUUID()

        val periode1 = Pair(OffsetDateTime.now().minusDays(4), OffsetDateTime.now().minusDays(2))
        val periode2 = Pair(OffsetDateTime.now().minusDays(1), null)

        val oppfolgingsperiodeDTOer = listOf(
            OppfolgingsperiodeDTO(uuid1, "aktorid", null, periode1.first, periode1.second),
            OppfolgingsperiodeDTO(uuid2, "aktorid", null, periode2.first, periode2.second)
        )

        val oppfolgingsperioder1: Oppfolgingsperioder =
            mapOppfolgingsperioder(oppfolgingsperiodeDTOer, null, null)

        SoftAssertions().apply {
            assertThat(oppfolgingsperioder1.oppfolgingsperioder).hasSize(2)
            assertThat(oppfolgingsperioder1.oppfolgingsperioder!![0].startDato).isEqualTo(periode1.first)
            assertThat(oppfolgingsperioder1.oppfolgingsperioder!![1].sluttDato).isEqualTo(periode2.second)
            assertThat(oppfolgingsperioder1.feil).isNull()
        }.assertAll()

        val internAktivitet = InternAktivitetBuilder.nyAktivitet("ijobb").oppfolgingsperiodeId(uuid2)
        val interneAktiviteter = listOf(
            InternAktivitetBuilder.nyAktivitet("egenaktivitet").oppfolgingsperiodeId(uuid1),
            InternAktivitetBuilder.nyAktivitet("mote").oppfolgingsperiodeId(uuid1),
            internAktivitet
        )

        val interneDialoger = listOf(
            InternDialogBuilder.nyDialog().oppfolgingsperiodeId(uuid2).aktivitetId(internAktivitet.aktivitetId),
            InternDialogBuilder.nyDialog().oppfolgingsperiodeId(uuid2)
        )

        val oppfolgingsperioder2: Oppfolgingsperioder =
            mapOppfolgingsperioder(oppfolgingsperiodeDTOer, interneAktiviteter, interneDialoger)

        assertThat(oppfolgingsperioder2.oppfolgingsperioder!![0].aktiviteter).hasSize(2)
        assertThat(oppfolgingsperioder2.oppfolgingsperioder!![1].aktiviteter).hasSize(1)
        assertThat(oppfolgingsperioder2.oppfolgingsperioder!![1].aktiviteter!![0].ijobb.dialog).isNotNull

        val oppfolgingsperioder3: Oppfolgingsperioder =
            mapOppfolgingsperioder(null, interneAktiviteter, interneDialoger)

        assertThat(oppfolgingsperioder3.oppfolgingsperioder).hasSize(1)
    }
}