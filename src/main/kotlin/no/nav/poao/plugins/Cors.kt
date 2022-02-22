package no.nav.poao.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*

fun Application.configureCors() {
    install(CORS) {
        host("app-q1.dev.adeo.no", schemes = listOf("http","https"))
        method(HttpMethod.Get)
        method(HttpMethod.Options)
        header(HttpHeaders.Origin)
        header(HttpHeaders.ContentType)
        header(HttpHeaders.Accept)

        allowCredentials = true
    }
}