package blazern.lexisoup.domain.model

data class Suggestion(
    val text: String,
    val lang: Lang,
    val translations: List<Sentence>,
)
