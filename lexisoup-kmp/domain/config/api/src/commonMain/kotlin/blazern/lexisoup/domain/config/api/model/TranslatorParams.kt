package blazern.lexisoup.domain.config.api.model

data class TranslatorParams(
    val translatorId: String,
    val minQueryLength: Int,
    val maxQueryLength: Int,
)
