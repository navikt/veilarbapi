package no.nav.poao.rest

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.common.types.identer.NavIdent
import no.nav.poao.util.setupEnvironment
import no.nav.poao.util.withWiremockServer
import no.nav.poao.veilarbapi.module
import no.nav.poao.veilarbapi.oppfolging.OppfolgingsenhetDTO
import no.nav.poao.veilarbapi.oppfolging.UnderOppfolgingDTO
import no.nav.poao.veilarbapi.oppfolging.VeilederDTO
import no.nav.security.mock.oauth2.withMockOAuth2Server
import no.nav.veilarbapi.model.Oppfolgingsinfo
import org.assertj.core.api.Assertions
import org.junit.Test
import kotlin.test.assertEquals

class ArbeidsoppfolgingRoutesITest {


    @Test
    fun `hent oppfolgingsinfo`() {

        val underOppfolgingDto = UnderOppfolgingDTO(true)
        val veilederDTO = VeilederDTO(NavIdent("z999999"))
        val oppfolgingsenhetDTO = OppfolgingsenhetDTO(navn = "Nav Grunerløkka", "1234")

        withWiremockServer {
            withMockOAuth2Server {
                val token = this.issueToken(subject = "enduser", audience = "clientid")
                withTestApplication({
                    setupEnvironment(this@withMockOAuth2Server, this@withWiremockServer)
                    module()
                    stubUnderOppfolging(underOppfolgingDto)
                    stubVeileder(veilederDTO)
                    stubOppfolgingsEnhet(oppfolgingsenhetDTO)
                }) {
                    with(handleRequest(HttpMethod.Get, "/v1/oppfolging/info?aktorId=123") {
                        addHeader(HttpHeaders.Authorization, "Bearer ${token.serialize()}")

                    }) {
                        assertEquals(HttpStatusCode.OK, response.status())
                        val oppfolgingsinfo =
                            no.nav.veilarbapi.JSON.deserialize<Oppfolgingsinfo>(
                                response.content,
                                Oppfolgingsinfo::class.java
                            )
                        Assertions.assertThat(oppfolgingsinfo.underOppfolging).isEqualTo(underOppfolgingDto.erUnderOppfolging)
                    }
                }

            }

        }
    }

     fun stubOppfolgingsEnhet(oppfolgingsenhetDTO: OppfolgingsenhetDTO) {
         val oppfolgingsenhetMock = Gson().toJson(oppfolgingsenhetDTO)
         stubFor(
             get(urlPathEqualTo("/veilarboppfolging/api/person/oppfolgingsenhet"))
                 .withQueryParam("aktorId", equalTo("123"))
                 .willReturn(
                     aResponse()
                         .withStatus(200)
                         .withBody(oppfolgingsenhetMock)
                 )
         )
     }

    fun stubVeileder(veilederDTO: VeilederDTO) {
        val veilederMock = Gson().toJson(veilederDTO)
        stubFor(
            get(urlPathEqualTo("/veilarboppfolging/api/v2/veileder"))
                .withQueryParam("aktorId", equalTo("123"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(veilederMock)
                )
        )
    }
    fun stubUnderOppfolging(underOppfolgingDTO: UnderOppfolgingDTO) {
        val underOppfolgingMock = Gson().toJson(underOppfolgingDTO)
        stubFor(
            get(urlPathEqualTo("/veilarboppfolging/api/v2/oppfolging"))
                .withQueryParam("aktorId", equalTo("123"))
                .willReturn(
                    aResponse()
                        .withStatus(200)
                        .withBody(underOppfolgingMock)
                )
        )




    }

}