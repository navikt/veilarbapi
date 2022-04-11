package no.nav.poao.veilarbapi.setup.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.http.*
import io.ktor.response.*
import no.nav.poao.veilarbapi.setup.exceptions.IkkePaaLoggetException
import no.nav.poao.veilarbapi.setup.exceptions.ManglerTilgangException
import no.nav.poao.veilarbapi.setup.exceptions.ServerFeilException

fun Application.configureExceptionHandler() {

    install(StatusPages) {
        exception<IkkePaaLoggetException> { cause ->
            call.respond(HttpStatusCode.Unauthorized)
            log.info("Ikke pålogget", cause)
        }
        exception<ManglerTilgangException> { cause ->
            call.respond(HttpStatusCode.Forbidden)
            log.info("Mangler tilgang", cause)
        }
        exception<ServerFeilException> { cause ->
            call.respond(HttpStatusCode.InternalServerError)
            log.warn("Serverfeil i klientkall", cause)
        }
    }

}