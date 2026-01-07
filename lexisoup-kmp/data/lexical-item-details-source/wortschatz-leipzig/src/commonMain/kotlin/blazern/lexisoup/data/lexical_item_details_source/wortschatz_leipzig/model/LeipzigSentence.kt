package blazern.lexisoup.data.lexical_item_details_source.wortschatz_leipzig.model

import kotlinx.serialization.Serializable

@Serializable
internal data class LeipzigSentence(
    val id: String,
    val sentence: String,
    val source: LeipzigSource? = null
)
