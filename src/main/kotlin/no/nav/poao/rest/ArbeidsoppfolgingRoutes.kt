package no.nav.poao.rest

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.poao.auth.MockPayload
import no.nav.poao.plugins.getMockAktiviteter
import no.nav.poao.plugins.getMockOppfolgingsinfo
import no.nav.poao.plugins.getMockOppfolgingsperioder

fun Application.arbeidsoppfolgingRoutes(useAuthentication: Boolean) {
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

fun Route.conditionalAuthenticate(useAuthentication: Boolean, build: Route.() -> Unit): Route {
    if (useAuthentication) {
        return authenticate(build = build, configurations = arrayOf("AzureAD"))
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
