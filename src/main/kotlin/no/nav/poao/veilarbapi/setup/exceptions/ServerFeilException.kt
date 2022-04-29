package no.nav.poao.veilarbapi.setup.exceptions

import io.ktor.client.statement.*

class ServerFeilException(val exceptionResponse: HttpResponse, val exceptionResponseText: String): Exception("Serverfeil i klientkall") {
}