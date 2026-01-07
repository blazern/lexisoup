package blazern.lexisoup.data.lexical_item_details_source.tatoeba.api

import kotlinx.serialization.Serializable

@Serializable
internal data class SentencePaging(
    val page: Int,
    val current: Int,
    val count: Int,
    val perPage: Int,
    val start: Int,
    val end: Int,
    val prevPage: Boolean,
    val nextPage: Boolean,
    val pageCount: Int,
    val sort: String? = null,
    val direction: String? = null,
    val limit: Int? = null,
    val sortDefault: Boolean,
    val directionDefault: Boolean,
    val scope: String? = null,
)
