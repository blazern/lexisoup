package blazern.lexisoup.data.kaikki.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Entry(
    val word: String,
    val pos: String,
    @SerialName("pos_title")
    val posTitle: String? = null,
    @SerialName("lang_code")
    val langCode: String,
    val lang: String,

    val senses: List<Sense> = emptyList(),
    val translations: List<Translation> = emptyList(),
    val sounds: List<Sound> = emptyList(),

    val antonyms: List<Related> = emptyList(),
    val hypernyms: List<Related> = emptyList(),
    val synonyms: List<Related> = emptyList(),
    @SerialName("coordinate_terms")
    val coordinateTerms: List<Related> = emptyList(),

    val tags: List<String> = emptyList(),
    val forms: List<Form> = emptyList()
)
