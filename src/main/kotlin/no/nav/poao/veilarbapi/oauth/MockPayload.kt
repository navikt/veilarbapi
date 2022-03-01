package no.nav.poao.veilarbapi.oauth

import com.auth0.jwt.interfaces.Claim
import com.auth0.jwt.interfaces.Payload

import java.util.*


class MockPayload(val staticSubject: String) : Payload {
    override fun getSubject(): String {
        return staticSubject
    }

    override fun getExpiresAt(): Date {
        TODO("not implemented")
    }

    override fun getIssuer(): String {
        TODO("not implemented")
    }

    override fun getAudience(): MutableList<String> {
        TODO("not implemented")
    }

    override fun getId(): String {
        TODO("not implemented")
    }

    override fun getClaims(): MutableMap<String, Claim> {
        TODO("not implemented")
    }

    override fun getIssuedAt(): Date {
        TODO("not implemented")
    }

    override fun getClaim(name: String?): Claim {
        TODO("not implemented")
    }

    override fun getNotBefore(): Date {
        TODO("not implemented")
    }
}

