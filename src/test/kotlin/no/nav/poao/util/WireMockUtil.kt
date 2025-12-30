package no.nav.poao.util

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import kotlinx.serialization.json.Json
import no.nav.poao.veilarbapi.oppfolging.OppfolgingsenhetDTO
import no.nav.poao.veilarbapi.oppfolging.UnderOppfolgingDTO
import no.nav.poao.veilarbapi.oppfolging.VeilederDTO

internal fun <R> withWiremockServer(
    test: WireMockServer.() -> R
): R {
    val server = WireMockServer(WireMockConfiguration.DYNAMIC_PORT)
    server.start()
    try {
        return server.test()
    } finally {
        server.shutdown()
    }
}

internal fun stubVeileder(wireMockServer: WireMockServer, veilederDTO: VeilederDTO) {
    val veilederMock = Json.encodeToString(veilederDTO)
    wireMockServer.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/veilarboppfolging/api/v2/veileder"))
            .withQueryParam("aktorId", WireMock.equalTo("123"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody(veilederMock)
            )
    )
}

internal fun stubUnderOppfolging(wireMockServer: WireMockServer, underOppfolgingDTO: UnderOppfolgingDTO) {
    val underOppfolgingMock = Json.encodeToString(underOppfolgingDTO)
    wireMockServer.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/veilarboppfolging/api/v2/oppfolging"))
            .withQueryParam("aktorId", WireMock.equalTo("123"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody(underOppfolgingMock)
            )
    )
}

internal fun stubOppfolgingsEnhet(wireMockServer: WireMockServer, oppfolgingsenhetDTO: OppfolgingsenhetDTO) {
    val oppfolgingsenhetMock = Json.encodeToString(oppfolgingsenhetDTO)
    wireMockServer.stubFor(
        WireMock.get(WireMock.urlPathEqualTo("/veilarboppfolging/api/person/oppfolgingsenhet"))
            .withQueryParam("aktorId", WireMock.equalTo("123"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody(oppfolgingsenhetMock)
            )
    )
}
