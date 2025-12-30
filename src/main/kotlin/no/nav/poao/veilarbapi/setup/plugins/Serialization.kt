package no.nav.poao.veilarbapi.setup.plugins

import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.application.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import no.nav.poao.veilarbapi.oppfolging.serdes.VeilarbapiSerializerModule


fun Application.configureSerialization() {
    install(ContentNegotiation) {
        json( Json {
            serializersModule = VeilarbapiSerializerModule
        } )
    }
}
