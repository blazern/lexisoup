package blazern.lexisoup.data.kaikki.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Sound(
    @SerialName("audio") val name: String? = null,
    @SerialName("ogg_url") val oggUrl: String? = null,
    @SerialName("mp3_url") val mp3Url: String? = null,
)
