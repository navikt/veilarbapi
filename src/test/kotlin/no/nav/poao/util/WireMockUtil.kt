package no.nav.poao.util

import com.expediagroup.graphql.client.types.GraphQLClientError
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import no.nav.poao.veilarbapi.oppfolging.OppfolgingsenhetDTO
import no.nav.poao.veilarbapi.oppfolging.OppfolgingsperiodeDTO

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

fun graphqlResponse(data: String, errors: List<GraphQLClientError>? = null): String {
    return """{
        "data": ${data},
        "errors": ${if (errors != null) """
            [
                ${errors.joinToString(", ") { "{ \"message\": \"${it.message}\", \"path\": [\"${it.path?.first()}\"] }" }}
            ]
        """.trimIndent() else "null" }
    }""".trimIndent()
}

fun oppfolgingsperioderResponse(perioder: List<OppfolgingsperiodeDTO>): String {
    return graphqlResponse("""
        { "oppfolgingsPerioder": [
            ${ perioder.joinToString(",") 
                { 
                    """
                       {
                            "id": "${it.uuid}",
                            "startTidspunkt": "${it.startDato?.toString()}",
                            "sluttTidspunkt": "${it.startDato?.toString()}",
                            "avsluttetAv": "${it.veileder}"
                       }
                    """.trimIndent()
                }
            }
        ] }
    """.trimIndent())
}

fun oppfolgingsInfoResponse(enhet: OppfolgingsenhetDTO?, veilederIdent: String?, errors: List<GraphQLClientError>? = null): String {
    return graphqlResponse("""{
          "oppfolgingsEnhet": {
              "enhet": ${
                  if (enhet != null) """
                      {
                        "id": "${enhet.enhetId}",
                        "navn": "${enhet.navn}"
                      }
                  """.trimIndent() else "null"
              } 
            },
            "oppfolging": {
                "erUnderOppfolging": true
            },
            "brukerStatus": {
                "veilederTilordning": {
                    "veilederIdent": ${if (veilederIdent != null) "\"${veilederIdent}\"" else "null"}
                }
            }
        }
    """.trimIndent(), errors)
}

internal fun stubOppfolgingsInfo(wireMockServer: WireMockServer, enhet: OppfolgingsenhetDTO, veilederIdent: String) {
    wireMockServer.stubFor(
        WireMock.post(WireMock.urlPathEqualTo("/veilarboppfolging/graphql"))
            .willReturn(
                WireMock.aResponse()
                    .withStatus(200)
                    .withBody(oppfolgingsInfoResponse(enhet, veilederIdent))
            )
    )
}