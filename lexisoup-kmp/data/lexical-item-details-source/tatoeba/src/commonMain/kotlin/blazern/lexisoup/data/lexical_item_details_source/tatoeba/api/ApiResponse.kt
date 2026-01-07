package blazern.lexisoup.data.lexical_item_details_source.tatoeba.api

import kotlinx.serialization.Serializable

@Serializable
internal data class ApiResponse(
    val paging: PagingWrapper? = null,
    val results: List<Sentence> = emptyList(),
)
