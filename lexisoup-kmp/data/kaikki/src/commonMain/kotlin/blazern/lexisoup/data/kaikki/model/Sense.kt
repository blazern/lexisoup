package blazern.lexisoup.data.kaikki.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Sense(
    val glosses: List<String> = emptyList(),
    val examples: List<Example> = emptyList(),
    @SerialName("form_of") val formOf: List<FormOf> = emptyList(),
)
