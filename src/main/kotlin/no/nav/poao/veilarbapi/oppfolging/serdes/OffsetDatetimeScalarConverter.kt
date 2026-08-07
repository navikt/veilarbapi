package no.nav.poao.veilarbapi.oppfolging.serdes

import com.expediagroup.graphql.client.converter.ScalarConverter
import java.time.OffsetDateTime

class OffsetDatetimeScalarConverter: ScalarConverter<OffsetDateTime> {
    override fun toJson(value: OffsetDateTime): Any {
        return value.toString()
    }

    override fun toScalar(rawValue: Any): OffsetDateTime {
        if (rawValue !is String) throw IllegalArgumentException("Expected String to be a OffsetDateTime")
        return OffsetDateTime.parse(rawValue)
    }
}