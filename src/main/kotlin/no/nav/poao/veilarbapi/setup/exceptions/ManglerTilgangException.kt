package no.nav.poao.veilarbapi.setup.exceptions

import io.ktor.client.statement.*

class ManglerTilgangException(val exceptionResponse: HttpResponse, val exceptionResponseText: String): Exception("Mangler tilgang") {
}