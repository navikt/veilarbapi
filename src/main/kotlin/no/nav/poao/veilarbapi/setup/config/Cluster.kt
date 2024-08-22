package no.nav.poao.veilarbapi.setup.config

enum class Cluster {
    DEV_GCP, PROD_GCP, LOKAL;

    companion object {
        val current: Cluster by lazy {
            when (val c = System.getenv("NAIS_CLUSTER_NAME")) {
                "dev-gcp" -> DEV_GCP
                "prod-gcp" -> PROD_GCP
                null -> LOKAL
                else -> throw RuntimeException("Ukjent cluster: $c")
            }
        }
    }

    fun toGcp(): String = when (this) {
        DEV_GCP -> "dev-gcp"
        PROD_GCP -> "prod-gcp"
        LOKAL -> "local"
    }

    fun toOnPrem(): String = when (this) {
        DEV_GCP -> "dev-fss"
        PROD_GCP -> "prod-fss"
        LOKAL -> "local"
    }

    fun asString(): String = name.lowercase().replace("_", "-")
}
