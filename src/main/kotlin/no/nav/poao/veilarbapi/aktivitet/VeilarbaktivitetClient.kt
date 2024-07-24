package no.nav.poao.veilarbapi.aktivitet

import no.nav.common.types.identer.AktorId
import no.nav.veilarbaktivitet.model.Aktivitet

interface VeilarbaktivitetClient {

    suspend fun hentAktiviteter(aktorId: AktorId, accessToken: String): Result<List<Aktivitet>>

}

