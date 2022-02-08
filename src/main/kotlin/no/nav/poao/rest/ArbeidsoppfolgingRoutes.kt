package no.nav.poao.rest

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.poao.plugins.getMockAktivitet
import no.nav.poao.plugins.getMockOppfolgingsperiode
import no.nav.poao.plugins.getMockOppfolgingsperioder

fun Application.arbeidsoppfolgingRoutes() {
    routing() {
        route("/v1/oppfolging/") {
            get("/periode") {
                call.respond(getMockOppfolgingsperioder(fromMockFile = true))
            }
            get("/periode/{oppfolgingsperiode_uuid}") {
                call.respond(getMockOppfolgingsperiode(fromMockFile = true))
            }
            get("/aktivitet?aktorId=(aktor_id)}") {
                call.respond(getMockAktivitet(fromMockFile = true))
            }
            get("/aktivitet/{aktivitet_id}") {
                call.respond(getMockAktivitet(fromMockFile = true))
            }
        }
    }
}