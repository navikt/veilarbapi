package no.nav.poao.veilarbapi.setup.rest

import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.common.types.identer.AktorId
import no.nav.poao.veilarbapi.oppfolging.OppfolgingService
import no.nav.poao.veilarbapi.setup.oauth.MockPayload
import no.nav.poao.veilarbapi.setup.util.getAccessToken
import no.nav.veilarbapi.model.Oppfolgingsinfo

fun Application.arbeidsoppfolgingRoutes(useAuthentication: Boolean, oppfolgingService: OppfolgingService) {
    routing() {
        conditionalAuthenticate(useAuthentication) {
            route("/v1/oppfolging/") {
                get("/periode") {
                    val aktorId = call.request.queryParameters["aktorId"]
                    if (aktorId == null) {
                        call.respond(HttpStatusCode.BadRequest, "AktorId er påkrevd")
                    } else {
                        log.info("Hent oppfølgingsperioder for aktorId: {}", aktorId)
                        val token = call.getAccessToken()
                        call.respond(oppfolgingService.fetchOppfolgingsPerioder(AktorId.of(aktorId), token))
                    }
                }
                get("info") {
                    val aktorId = call.request.queryParameters["aktorId"]
                    if (aktorId == null) {
                        call.respond(HttpStatusCode.BadRequest, "AktorId er påkrevd")
                    } else {
                        log.info("Hent oppfølgingsInfo for aktorId: {}", aktorId)
                        val token = call.getAccessToken()

                        val result: Result<Oppfolgingsinfo?> = oppfolgingService.fetchOppfolgingsInfo(AktorId.of(aktorId), token)

                        val resultSuccess = result.getOrThrow()
                        if (resultSuccess == null) {
                            call.respond(HttpStatusCode.NoContent)
                        } else {
                            call.respond(resultSuccess)
                        }
                    }
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
