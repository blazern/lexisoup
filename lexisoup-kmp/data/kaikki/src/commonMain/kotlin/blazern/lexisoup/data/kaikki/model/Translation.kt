package blazern.lexisoup.data.kaikki.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Translation(
    val sense: String? = null,
    val word: String? = null,
    @SerialName("lang_code")
    val langCode: String? = null,
    val tags: List<String>? = null
)
