package no.nav.poao.veilarbapi.plugins

import io.ktor.application.*
import no.nav.poao.veilarbapi.client.VeilarbaktivitetClient
import no.nav.poao.veilarbapi.client.VeilarbdialogClient
import no.nav.poao.veilarbapi.rest.arbeidsoppfolgingRoutes
import no.nav.poao.veilarbapi.rest.internalRoutes

fun Application.configureRouting(useAuthentication: Boolean, veilarbaktivitetClient: VeilarbaktivitetClient, veilarbdialogClient: VeilarbdialogClient) {
    internalRoutes()
    arbeidsoppfolgingRoutes(useAuthentication, veilarbaktivitetClient = veilarbaktivitetClient, veilarbdialogClient)
}
