package blazern.lexisoup.data.lexical_item_details_source.wortschatz_leipzig.model

import kotlinx.serialization.Serializable

@Serializable
internal data class LeipzigSentencesResponse(
    val count: Int? = null,
    val sentences: List<LeipzigSentence> = emptyList()
)
