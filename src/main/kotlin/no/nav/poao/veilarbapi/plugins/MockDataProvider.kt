package no.nav.poao.veilarbapi.plugins

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

fun Application.getMockAktiviteter(fromMockFile: Boolean): Array<Aktivitet> {
    if (fromMockFile) {
        val mockDataFileName = "mock/aktiviteter.json"
        val json = this::class.java.classLoader.getResource(mockDataFileName).readText(Charsets.UTF_8)
        return JSON.deserialize(json, Aktivitet::class.java.arrayType())
    } else return arrayOf(Aktivitet(mote))
}

fun Application.getMockOppfolgingsinfo(fromMockFile: Boolean): Oppfolgingsinfo {
    if (fromMockFile) {
        val mockDataFileName = "mock/oppfolgingsinfo.json"
        val json = this::class.java.classLoader.getResource(mockDataFileName).readText(Charsets.UTF_8)
        return JSON.deserialize(json, Oppfolgingsinfo::class.java)
    } else return oppfolgingsinfo
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
    .addAktiviteterItem(Aktivitet(mote))

val oppfolgingsinfo = Oppfolgingsinfo()
    .underOppfolging(true)
    .primaerVeileder("Z999999")