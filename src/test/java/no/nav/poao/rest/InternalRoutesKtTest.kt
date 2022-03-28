package no.nav.poao.rest

import io.ktor.http.*

import kotlin.test.*
import io.ktor.server.testing.*
import no.nav.poao.veilarbapi.setup.rest.internalRoutes

class InternalRoutesKtTest {
    @Test
    fun testPing() {
        withTestApplication({ internalRoutes() }) {
            handleRequest(HttpMethod.Get, "/internal/isAlive").apply {
                assertEquals(HttpStatusCode.OK, response.status())
            }
        }
    }
}
