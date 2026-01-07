package blazern.lexisoup.data.lexical_item_details_source.tatoeba.api

import kotlinx.serialization.Serializable

@Serializable
internal data class Translation(
    val text: String,
    val lang: String,
    val isDirect: Boolean? = null,
)
