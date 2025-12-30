package no.nav.poao.veilarbapi.setup.plugins

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.metrics.micrometer.*
import io.ktor.server.plugins.calllogging.CallLogging
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry

fun Application.configureMonitoring() {
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry

        timers { call, exception ->
            tag("path", call.request.path())
            if (exception != null) tag("exception", exception::class.simpleName)
        }
    }

    routing {
        get("/internal/prometheus") {
            call.respond(appMicrometerRegistry.scrape())
        }
    }


    install(CallLogging) {
        mdc("Nav-Consumer-Id") { call ->
            call.request.header("Nav-Consumer-Id")
        }
        mdc("Nav-Call-Id") { call ->
            call.request.header("Nav-Call-Id")
        }
        filter { call ->
            !call.request.path().startsWith("/internal")
        }
        format { call ->
            val status = call.response.status()
            val httpMethod = call.request.httpMethod.value
            val uri = call.request.uri
            val userAgent = call.request.headers["User-Agent"]
            "Url: $uri, HTTP method: $httpMethod, Status: $status, User agent: $userAgent"
        }
    }

}
