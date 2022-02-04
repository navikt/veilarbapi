package no.nav.poao.plugins

import io.ktor.application.*
import no.nav.veilarbapi.JSON
import no.nav.veilarbapi.model.Oppfolgingsperiode

fun Application.getMockData(): Oppfolgingsperiode {
    val mockDataFileName = "mock.json"
    val json = this::class.java.classLoader.getResource(mockDataFileName).readText(Charsets.UTF_8)
    JSON()
    return JSON.deserialize<Oppfolgingsperiode>(json, Oppfolgingsperiode::class.java)
}