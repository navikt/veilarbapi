package no.nav.poao.util

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.google.gson.Gson
import no.nav.poao.veilarbapi.oppfolging.OppfolgingsenhetDTO
import no.nav.poao.veilarbapi.oppfolging.UnderOppfolgingDTO
import no.nav.poao.veilarbapi.oppfolging.VeilederDTO

internal fun <R> withWiremockServer(
    test: WireMockServer.() -> R
): R {
    val server = WireMockServer(0)
    server.start()
    try {
        return server.test()
    } finally {
        server.shutdown()
    }
}


fun stubVeileder(veilederDTO: VeilederDTO) {
    val veilederMock = Gson().toJson(veilederDTO)
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/veilarboppfolging/api/v2/veileder"))
            .withQueryParam("aktorId", WireMock.equalTo("123"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody(veilederMock)
            )
    )
}

fun stubUnderOppfolging(underOppfolgingDTO: UnderOppfolgingDTO) {
    val underOppfolgingMock = Gson().toJson(underOppfolgingDTO)
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/veilarboppfolging/api/v2/oppfolging"))
            .withQueryParam("aktorId", WireMock.equalTo("123"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody(underOppfolgingMock)
            )
    )
}

fun stubOppfolgingsEnhet(oppfolgingsenhetDTO: OppfolgingsenhetDTO) {
    val oppfolgingsenhetMock = Gson().toJson(oppfolgingsenhetDTO)
    WireMock.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/veilarboppfolging/api/person/oppfolgingsenhet"))
            .withQueryParam("aktorId", WireMock.equalTo("123"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody(oppfolgingsenhetMock)
            )
    )
}