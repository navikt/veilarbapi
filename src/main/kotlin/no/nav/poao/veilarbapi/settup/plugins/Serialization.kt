package no.nav.poao.veilarbapi.settup.plugins

import io.ktor.features.*
import io.ktor.application.*
import io.ktor.gson.*
import io.ktor.http.*
import no.nav.veilarbapi.JSON

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        JSON()
        val converter = GsonConverter(JSON.getGson())
        register(ContentType.Application.Json, converter)
    }
}
