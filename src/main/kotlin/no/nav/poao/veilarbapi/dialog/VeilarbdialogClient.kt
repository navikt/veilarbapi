import no.nav.common.types.identer.AktorId
import no.nav.veilarbdialog.model.Dialog

interface VeilarbdialogClient {

    suspend fun hentDialoger(aktorId: AktorId, accessToken: String?): Result<List<Dialog>>

}
