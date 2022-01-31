package no.nav.poao

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import no.nav.poao.plugins.*

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0") {
        configureMonitoring()
        configureRouting()
        configureSerialization()
    }.start(wait = true)
}
