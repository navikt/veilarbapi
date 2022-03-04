package no.nav.poao.rest


import io.ktor.http.*

import kotlin.test.*
import io.ktor.server.testing.*
import no.nav.poao.veilarbapi.client.VeilarbaktivitetClient
import no.nav.poao.veilarbapi.client.VeilarbdialogClient
import no.nav.poao.veilarbapi.config.Configuration
import no.nav.poao.veilarbapi.plugins.*
import no.nav.poao.veilarbapi.plugins.configureRouting
import no.nav.poao.veilarbapi.plugins.configureSerialization
import no.nav.veilarbapi.JSON
import no.nav.veilarbapi.model.Aktivitet
import no.nav.veilarbapi.model.Mote
import no.nav.veilarbapi.model.Oppfolgingsinfo
import no.nav.veilarbapi.model.Oppfolgingsperiode
import org.assertj.core.api.Assertions
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat

class ArbeidsoppfolgingRoutesKtTest {
    val veilarbaktivitetClient = VeilarbaktivitetClient(
        Configuration.VeilarbaktivitetConfig(url="http://veilarbaktivitet", clientId = "clientid"),
        poaoGcpProxyConfig = Configuration.PoaoGcpProxyConfig(url="http://veilarbaktivitet", clientId = "clientid"),
        azureAdClient = null
    )
    val veilarbdialogClient = VeilarbdialogClient(
        Configuration.VeilarbdialogConfig(url="http://veilarbaktivitet", clientId = "clientid"),
        azureAdClient = null
    )
    @Test
    fun testMockOppfolgingsperioder() {
        val expectedMockDataFile = "mock/oppfolgingperioder.json"
        val json = this::class.java.classLoader.getResource(expectedMockDataFile)
            .readText(Charsets.UTF_8)
        JSON()

        val expectedOppfolgingsperioder = JSON.deserialize<Array<Oppfolgingsperiode>>(json, Oppfolgingsperiode::class.java.arrayType())
        withTestApplication({
            configureRouting(false, veilarbaktivitetClient = veilarbaktivitetClient, veilarbdialogClient = veilarbdialogClient)
            configureSerialization()
        }) {
            handleRequest(HttpMethod.Get, "/v1/oppfolging/periode") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val oppfolgingsperioder = JSON.deserialize<Array<Oppfolgingsperiode>>(response.content, Oppfolgingsperiode::class.java.arrayType())
                Assertions.assertThat(oppfolgingsperioder).containsAll(expectedOppfolgingsperioder.asIterable())
                val aktivitet = oppfolgingsperioder?.get(0)?.aktiviteter?.get(0)?.actualInstance
                assertThat(aktivitet, instanceOf(Mote::class.java))
            }
        }
    }

    @Test
    @Ignore
    fun testMockAktiviteter() {
        val expectedMockDataFile = "mock/aktiviteter.json"
        val json = this::class.java.classLoader.getResource(expectedMockDataFile)
            .readText(Charsets.UTF_8)
        JSON()

        val expectedAktiviteter = JSON.deserialize<Array<Aktivitet>>(json, Aktivitet::class.java.arrayType())
        withTestApplication({
            configureRouting(false, veilarbaktivitetClient = veilarbaktivitetClient, veilarbdialogClient = veilarbdialogClient)
            configureSerialization()
        }) {
            handleRequest(HttpMethod.Get, "/v1/oppfolging/aktivitet?aktorId=12345678") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val aktiviteter = JSON.deserialize<Array<Aktivitet>>(response.content, Aktivitet::class.java.arrayType())
                Assertions.assertThat(aktiviteter).containsAll(expectedAktiviteter.asIterable())
                val aktivitet = aktiviteter?.get(0)?.actualInstance
                assertThat(aktivitet, instanceOf(Mote::class.java))
            }
        }
    }

    @Test
    fun testMockOppfolgingsinfo() {
        val expectedMockDataFile = "mock/oppfolgingsinfo.json"
        val json = this::class.java.classLoader.getResource(expectedMockDataFile)
            .readText(Charsets.UTF_8)
        JSON()

        val expectedOppfolgingsinfo = JSON.deserialize<Oppfolgingsinfo>(json, Oppfolgingsinfo::class.java)
        withTestApplication({
            configureRouting(false, veilarbaktivitetClient = veilarbaktivitetClient, veilarbdialogClient = veilarbdialogClient)
            configureSerialization()
        }) {
            handleRequest(HttpMethod.Get, "/v1/oppfolging/info?aktorId=12345678") {
                addHeader(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            }.apply {
                assertEquals(HttpStatusCode.OK, response.status())
                val oppfolgingsinfo = JSON.deserialize<Oppfolgingsinfo>(response.content, Oppfolgingsinfo::class.java)
                Assertions.assertThat(oppfolgingsinfo).isEqualTo(expectedOppfolgingsinfo)
            }
        }
    }

}