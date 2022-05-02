package no.nav.poao.veilarbapi.setup.plugins

import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.*
import io.ktor.serialization.gson.*
import io.ktor.http.*
import no.nav.veilarbapi.JSON


fun Application.configureSerialization() {
    install(ContentNegotiation) {
        JSON()
        val converter = GsonConverter(JSON.getGson())
        register(ContentType.Application.Json, converter)
    }
}
