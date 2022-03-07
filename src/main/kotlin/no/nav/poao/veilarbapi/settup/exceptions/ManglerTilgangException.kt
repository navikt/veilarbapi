package no.nav.poao.veilarbapi.settup.exceptions

import io.ktor.client.statement.*

class ManglerTilgangException(val exceptionResponse: HttpResponse, val exceptionResponseText: String): Exception("Mangler tilgang") {
}