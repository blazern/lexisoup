package blazern.lexisoup.domain.model

data class Suggestion(
    val text: String,
    val translations: List<Sentence>,
)
