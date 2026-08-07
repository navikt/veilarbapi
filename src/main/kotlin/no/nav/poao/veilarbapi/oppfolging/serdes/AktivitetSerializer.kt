package no.nav.poao.veilarbapi.oppfolging.serdes

import kotlinx.serialization.DeserializationStrategy
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonContentPolymorphicSerializer
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import no.nav.veilarbaktivitet.model.Aktivitet
import no.nav.veilarbaktivitet.model.Behandling
import no.nav.veilarbaktivitet.model.Egenaktivitet
import no.nav.veilarbaktivitet.model.Ijobb
import no.nav.veilarbaktivitet.model.Jobbsoeking
import no.nav.veilarbaktivitet.model.Mote
import no.nav.veilarbaktivitet.model.Samtalereferat
import no.nav.veilarbaktivitet.model.Sokeavtale
import no.nav.veilarbaktivitet.model.StillingFraNav

object AktivitetSerializer : JsonContentPolymorphicSerializer<Aktivitet>(Aktivitet::class) {
    override fun selectDeserializer(element: JsonElement): DeserializationStrategy<Aktivitet> {
        // Manually check the exact key as it appears in the JSON
        val type = element.jsonObject["aktivitet_type"]?.jsonPrimitive?.content
        return when (type) {
            "egenaktivitet" -> Egenaktivitet.serializer()
            "behandling" -> Behandling.serializer()
            "mote" -> Mote.serializer()
            "samtalereferat" -> Samtalereferat.serializer()
            "sokeavtale" -> Sokeavtale.serializer()
            "stilling_fra_nav" -> StillingFraNav.serializer()
            "ijobb" -> Ijobb.serializer()
            "jobbsoeking" -> Jobbsoeking.serializer()
            else -> throw SerializationException("Unknown: $type")
        }
    }
}