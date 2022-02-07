package no.nav.poao.rest

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.poao.plugins.getMockData

fun Application.arbeidsoppfolgingRoutes() {
    routing() {
        route("/") {
            get("/arbeidsoppfolging") {
                call.respond(getMockData(fromMockFile = true))
            }
        }
    }
}