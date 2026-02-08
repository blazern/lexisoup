package blazern.lexisoup.domain.config.api.model

data class TranslatorParams(
    val translatorId: String,
    val textLengthMin: Int,
    val textLengthMax: Int,
    val batchSizeLimit: Int,
)
