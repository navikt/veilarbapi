package no.nav.poao

import io.ktor.http.*

import kotlin.test.*
import io.ktor.server.testing.*
import io.ktor.util.reflect.*
import no.nav.poao.plugins.*
import no.nav.veilarbapi.JSON
import no.nav.veilarbapi.model.Mote
import no.nav.veilarbapi.model.Oppfolgingsperiode
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat

class ApplicationTest {
    @Test
    fun testPing() {
        withTestApplication({ configureRouting() }) {
            handleRequest(HttpMethod.Get, "/internal/isAlive").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }

    @Test
    fun testMockResponse() {
        val expectedMockDataFile = "mock.json"
        val json = this::class.java.classLoader.getResource(expectedMockDataFile)
            .readText(Charsets.UTF_8)
        JSON()
        val expectedOppfolgingsperiode = JSON.deserialize<Oppfolgingsperiode>(json, Oppfolgingsperiode::class.java)
        withTestApplication({
            configureRouting()
            configureSerialization()
        }) {
            handleRequest(HttpMethod.Get, "/arbeidsoppfolging") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val oppfolgingsperiode = JSON.deserialize<Oppfolgingsperiode>(response.content, Oppfolgingsperiode::class.java)
                assertEquals(expectedOppfolgingsperiode, oppfolgingsperiode)
                val aktivitet = oppfolgingsperiode.aktiviteter?.get(0)?.actualInstance
                assertThat(aktivitet, instanceOf(Mote::class.java))
            }
        }
    }
}
