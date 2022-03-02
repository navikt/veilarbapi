package no.nav.poao.veilarbapi.plugins

import io.ktor.application.*
import io.ktor.features.*
import io.ktor.metrics.micrometer.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.micrometer.prometheus.PrometheusConfig
import io.micrometer.prometheus.PrometheusMeterRegistry

fun Application.configureMonitoring() {
    val appMicrometerRegistry = PrometheusMeterRegistry(PrometheusConfig.DEFAULT)
    install(MicrometerMetrics) {
        registry = appMicrometerRegistry
        routing {
            get("/internal/prometheus") {
                call.respond(appMicrometerRegistry.scrape())
            }
        }

        timers { call, exception ->
            tag("path", call.request.path())
            if (exception != null) tag("exception", exception::class.simpleName)
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
