package blazern.lexisoup.domain.config.api.model

data class Config(
    val backendRedirectionUrl: String?,
    val minQueryLength: Int,
    val maxQueryLength: Int,
    val translateTextLengthMax: Int,
    val translateTextLengthMin: Int,
    val translateBatchSizeLimit: Int,
)
