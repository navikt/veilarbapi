package no.nav.poao.veilarbapi.rest

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.poao.veilarbapi.client.VeilarbaktivitetClient
import no.nav.poao.veilarbapi.client.VeilarbdialogClient
import no.nav.poao.veilarbapi.getAccessToken
import no.nav.poao.veilarbapi.oauth.MockPayload
import no.nav.poao.veilarbapi.plugins.getMockOppfolgingsinfo
import no.nav.poao.veilarbapi.plugins.getMockOppfolgingsperioder

fun Application.arbeidsoppfolgingRoutes(useAuthentication: Boolean, veilarbaktivitetClient: VeilarbaktivitetClient, veilarbdialogClient: VeilarbdialogClient) {
    routing() {
        conditionalAuthenticate(useAuthentication) {
            route("/v1/oppfolging/") {
                get("/periode") {
                    val aktorId = call.request.queryParameters["aktorId"]
                    log.info("Hent oppfølgingsperioder for aktorId: {}", aktorId)
                    call.respond(getMockOppfolgingsperioder(fromMockFile = true))
                }
                get("/aktivitet") {
                    val aktorId = call.request.queryParameters["aktorId"]
                    if (aktorId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Missing aktorId")
                    } else {
                        val token = call.getAccessToken()
                        log.info("Hent aktiviteter for aktorId: {}", aktorId)
                        val aktiviteter = veilarbaktivitetClient.hentAktiviteter(aktorId, token)
                        call.respond(aktiviteter)
                    }
                }
                get("/dialog") {
                    val aktorId = call.request.queryParameters["aktorId"]
                    if (aktorId == null) {
                        call.respond(HttpStatusCode.BadRequest, "Missing aktorId")
                    } else {
                        val token = call.getAccessToken()
                        log.info("Hent aktiviteter for aktorId: {}", aktorId)
                        val dialoger = veilarbdialogClient.hentDialoger(aktorId, token)
                        call.respond(dialoger)
                    }
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

fun Route.conditionalAuthenticate(useAuthentication: Boolean, build: Route.() -> Unit): Route {
    if (useAuthentication) {
        return authenticate(build = build, configurations = arrayOf("azuread"))
    } else return mockAuthentication(build)
}

fun Route.mockAuthentication(build: Route.() -> Unit): Route {
    val route = createChild(AuthenticationRouteSelector(listOf<String?>(null)))
    route.insertPhaseAfter(ApplicationCallPipeline.Features, Authentication.AuthenticatePhase)
    route.intercept(Authentication.AuthenticatePhase) {
        this.context.authentication.principal = JWTPrincipal(MockPayload("Z999999"))
    }
    route.build()
    return route
}
