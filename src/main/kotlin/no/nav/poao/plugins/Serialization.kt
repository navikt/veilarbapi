package no.nav.poao.plugins

import io.ktor.serialization.*
import io.ktor.features.*
import io.ktor.application.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.request.*
import io.ktor.routing.*
import no.nav.veilarbapi.JSON

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        gson(block = {JSON.createGson()})
    }


    routing {
        get("/gson/kotlinx-serialization") {
            call.respond(mapOf("hello" to "world"))
        }
    }
}
