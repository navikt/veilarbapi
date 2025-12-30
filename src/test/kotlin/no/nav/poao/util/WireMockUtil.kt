package no.nav.poao.util

import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.install
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import io.ktor.server.testing.ApplicationTestBuilder
import no.nav.poao.veilarbapi.oppfolging.OppfolgingsenhetDTO
import no.nav.poao.veilarbapi.oppfolging.UnderOppfolgingDTO
import no.nav.poao.veilarbapi.oppfolging.VeilederDTO

fun ApplicationTestBuilder.stubVeileder(veilederDTO: VeilederDTO) {
    externalServices {
        hosts("veilarboppfolging") {
            this@hosts.install(ContentNegotiation) {
                json()
            }
            this@hosts.routing {
                get("/veilarboppfolging/api/v2/veileder") {
                    call.respond(veilederDTO)
                }
            }
        }
    }
}

fun ApplicationTestBuilder.stubUnderOppfolging(underOppfolgingDTO: UnderOppfolgingDTO) {
    externalServices {
        hosts("veilarboppfolging") {
            this@hosts.install(ContentNegotiation) {
                json()
            }
            this@hosts.routing {
                 get("/veilarboppfolging/api/v2/oppfolging") {
                    call.respond(underOppfolgingDTO)
                }
            }
        }
    }
}

fun ApplicationTestBuilder.stubOppfolgingsEnhet(oppfolgingsenhetDTO: OppfolgingsenhetDTO) {
    externalServices {
        hosts("veilarboppfolging") {
            this@hosts.install(ContentNegotiation) {
                json()
            }
            this@hosts.routing {
                get("/veilarboppfolging/api/person/oppfolgingsenhet") {
                    call.respond(oppfolgingsenhetDTO)
                }
            }
        }
    }
}

