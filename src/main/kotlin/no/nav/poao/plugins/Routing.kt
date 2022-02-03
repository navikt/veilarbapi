package no.nav.poao.plugins

import io.ktor.routing.*
import io.ktor.application.*
import io.ktor.response.*
import no.nav.veilarbapi.model.Aktivitet
import no.nav.veilarbapi.model.Mote
import no.nav.veilarbapi.model.Oppfolgingsperiode

fun Application.configureRouting() {



    routing {
        get("/arbeidsoppfolging") {

            val oppfolgingsperiode = Oppfolgingsperiode()
            val mote = Mote()
            oppfolgingsperiode.addAktiviteterItem(Aktivitet( mote))
            oppfolgingsperiode
            call.respond(oppfolgingsperiode)
        }
    }
}
