package no.nav.poao.plugins

import io.ktor.application.*
import no.nav.veilarbapi.JSON
import no.nav.veilarbapi.model.*
import org.threeten.bp.OffsetDateTime
import java.util.*

fun Application.getMockOppfolgingsperioder(fromMockFile: Boolean): Array<Oppfolgingsperiode> {
    if (fromMockFile) {
        val mockDataFileName = "mock/oppfolgingperioder.json"
        val json = this::class.java.classLoader.getResource(mockDataFileName).readText(Charsets.UTF_8)
        return JSON.deserialize(json, Oppfolgingsperiode::class.java.arrayType())
    } else return arrayOf(oppfolgingsperiode)
}

fun Application.getMockOppfolgingsperiode(fromMockFile: Boolean): Oppfolgingsperiode {
    if (fromMockFile) {
        val mockDataFileName = "mock/oppfolgingperiode.json"
        val json = this::class.java.classLoader.getResource(mockDataFileName).readText(Charsets.UTF_8)
        return JSON.deserialize(json, Oppfolgingsperiode::class.java)
    } else return oppfolgingsperiode
}

fun Application.getMockAktiviteter(fromMockFile: Boolean): Array<Aktivitet> {
    if (fromMockFile) {
        val mockDataFileName = "mock/aktiviteter.json"
        val json = this::class.java.classLoader.getResource(mockDataFileName).readText(Charsets.UTF_8)
        return JSON.deserialize(json, Aktivitet::class.java.arrayType())
    } else return arrayOf(Aktivitet(mote))
}

fun Application.getMockAktivitet(fromMockFile: Boolean): Aktivitet {
    if (fromMockFile) {
        val mockDataFileName = "mock/aktivitet.json"
        val json = this::class.java.classLoader.getResource(mockDataFileName).readText(Charsets.UTF_8)
        return JSON.deserialize(json, Aktivitet::class.java)
    } else return Aktivitet(mote)
}

val mote: Mote = Mote()
    .referat("Vi pratet om litt av hvert")
    .adresse("Nav Sandaker")
    .forberedelser("Agenda")
    .endretDato(OffsetDateTime.now().minusDays(2))
    .fraDato(OffsetDateTime.now())
    .tilDato(OffsetDateTime.now().plusDays(2))
    .avtaltMedNav(true)
    .tittel("Møte")
    .beskrivelse("Beste møtet ever")
    .opprettetDato(OffsetDateTime.now()) as Mote

val oppfolgingsperiode = Oppfolgingsperiode()
    .id(UUID.randomUUID())
    .addAktiviteterItem(Aktivitet(mote))