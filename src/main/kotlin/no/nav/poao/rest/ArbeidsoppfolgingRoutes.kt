package no.nav.poao.rest

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.poao.plugins.getMockAktiviteter
import no.nav.poao.plugins.getMockOppfolgingsinfo
import no.nav.poao.plugins.getMockOppfolgingsperioder

fun Application.arbeidsoppfolgingRoutes() {
    routing() {
        authenticate {
            route("/v1/oppfolging/") {
                get("/periode") {
                    val aktorId = call.request.queryParameters["aktorId"]
                    log.info("Hent oppfølgingsperioder for aktorId: {}", aktorId)
                    call.respond(getMockOppfolgingsperioder(fromMockFile = true))
                }
                get("/aktivitet") {
                    val aktorId = call.request.queryParameters["aktorId"]
                    log.info("Hent aktiviteter for aktorId: {}", aktorId)
                    call.respond(getMockAktiviteter(fromMockFile = true))
                }
                get("info") {
                    val aktorId = call.request.queryParameters["aktorId"]
                    log.info("Hent oppfølgingsInfo for aktorId: {}", aktorId)
                    call.respond(getMockOppfolgingsinfo(fromMockFile = true))
                }
            }
        }
    }
}
