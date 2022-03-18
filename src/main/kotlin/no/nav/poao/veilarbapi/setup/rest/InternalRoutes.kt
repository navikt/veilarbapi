package no.nav.poao.veilarbapi.setup.rest

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

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