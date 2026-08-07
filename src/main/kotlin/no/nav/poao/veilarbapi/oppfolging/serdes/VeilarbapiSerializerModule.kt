package no.nav.poao.veilarbapi.oppfolging.serdes

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import no.nav.veilarbaktivitet.model.Aktivitet
import java.time.LocalDate
import java.time.OffsetDateTime
import java.util.UUID

val VeilarbapiSerializerModule = SerializersModule {
    contextual(UUID::class) { UUIDSerializer }
    contextual(OffsetDateTime::class) { OffsetDateTimeSerializer }
    contextual(LocalDate::class) { LocalDateSerializer }
    polymorphic(Aktivitet::class) {
        defaultDeserializer { AktivitetSerializer }
    }
}