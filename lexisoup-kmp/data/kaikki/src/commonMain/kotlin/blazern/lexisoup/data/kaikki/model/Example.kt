package blazern.lexisoup.data.kaikki.model

import kotlinx.serialization.Serializable

@Serializable
data class Example(
    val text: String,
    val ref: String? = null,
    val author: String? = null
)
