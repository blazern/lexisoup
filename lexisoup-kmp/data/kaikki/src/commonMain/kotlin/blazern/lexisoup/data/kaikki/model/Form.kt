package blazern.lexisoup.data.kaikki.model

import kotlinx.serialization.Serializable

@Serializable
data class Form(
    val form: String,
    val tags: List<String>? = null
)
