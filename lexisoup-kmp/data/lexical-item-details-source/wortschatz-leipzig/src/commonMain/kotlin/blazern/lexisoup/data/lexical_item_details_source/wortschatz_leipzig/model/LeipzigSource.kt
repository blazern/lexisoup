package blazern.lexisoup.data.lexical_item_details_source.wortschatz_leipzig.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class LeipzigSource(
    val id: String? = null,
    val url: String? = null,
    // The API returns ISO date strings like "2013-04-27"
    @SerialName("date") val date: String? = null
)
