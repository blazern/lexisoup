package blazern.lexisoup.domain.config.api.model

data class Config(
    val backendRedirectionUrl: String?,
    val minQueryLength: Int,
    val maxQueryLength: Int,
    val translatorsParams: Map<String, TranslatorParams>,
)
