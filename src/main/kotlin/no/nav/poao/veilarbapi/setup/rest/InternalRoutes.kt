package no.nav.poao.veilarbapi.setup.rest

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Application.internalRoutes() {
    routing() {
        route("/internal") {
            route("/isAlive") {
                get {
                    call.respond(HttpStatusCode.OK)
                }
            }

            route("/isReady") {
                get {
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}