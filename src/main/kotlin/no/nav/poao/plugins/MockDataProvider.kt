package no.nav.poao.plugins

import io.ktor.application.*
import no.nav.veilarbapi.JSON
import no.nav.veilarbapi.model.*
import org.threeten.bp.OffsetDateTime
import java.util.*

fun Application.getMockData(fromMockFile: Boolean): Oppfolgingsperiode {
    if (fromMockFile) {
        val mockDataFileName = "mock.json"
        val json = this::class.java.classLoader.getResource(mockDataFileName).readText(Charsets.UTF_8)
        return JSON.deserialize(json, Oppfolgingsperiode::class.java)
    } else return oppfolgingsperiode()
}

fun oppfolgingsperiode() : Oppfolgingsperiode {
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
    return oppfolgingsperiode
}
