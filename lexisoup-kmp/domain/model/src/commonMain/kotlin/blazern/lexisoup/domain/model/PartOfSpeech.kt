package blazern.lexisoup.domain.model


sealed interface PartOfSpeech {
    object Noun : PartOfSpeech
    object Verb : PartOfSpeech

    data class Other(
        val raw: String,
    ) : PartOfSpeech
}
