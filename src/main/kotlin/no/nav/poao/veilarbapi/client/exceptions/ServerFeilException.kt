package no.nav.poao.veilarbapi.client.exceptions

import io.ktor.client.statement.*

class ServerFeilException(val exceptionResponse: HttpResponse, val exceptionResponseText: String): Exception("Mangler tilgang") {
}