package no.nav.poao.veilarbapi.setup.exceptions

import io.ktor.client.statement.*

class IkkePaaLoggetException(val exceptionResponse: HttpResponse, val exceptionResponseText: String): Exception("Ikke pålogget") {
}