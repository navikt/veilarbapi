package no.nav.poao.veilarbapi.setup.plugins

import io.ktor.server.application.*
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.plugins.statuspages.*
import no.nav.poao.veilarbapi.setup.exceptions.IkkePaaLoggetException
import no.nav.poao.veilarbapi.setup.exceptions.ManglerTilgangException
import no.nav.poao.veilarbapi.setup.exceptions.EksternServerFeilException

fun Application.configureExceptionHandler() {

    install(StatusPages) {
        exception<IkkePaaLoggetException> { call, cause ->
            call.respond(HttpStatusCode.Unauthorized)
            call.application.log.info("Ikke pålogget", cause)
        }
        exception<ManglerTilgangException> { call, cause ->
            call.respond(HttpStatusCode.Forbidden)
            call.application.log.info("Mangler tilgang", cause)
        }
        exception<EksternServerFeilException> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError)
            call.application.log.warn("Serverfeil i klientkall", cause)
        }
    }

}