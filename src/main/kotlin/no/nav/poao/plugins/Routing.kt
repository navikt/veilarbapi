package no.nav.poao.plugins

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import no.nav.veilarbapi.JSON
import no.nav.veilarbapi.model.Aktivitet
import no.nav.veilarbapi.model.Mote
import no.nav.veilarbapi.model.Oppfolgingsperiode

fun Application.configureRouting() {
    routing {
        get("ping") {
            call.respond(HttpStatusCode.OK, "pong")
        }
        get("/arbeidsoppfolging") {
            call.respond(getMockData(fromMockFile = true))
        }
    }
}
