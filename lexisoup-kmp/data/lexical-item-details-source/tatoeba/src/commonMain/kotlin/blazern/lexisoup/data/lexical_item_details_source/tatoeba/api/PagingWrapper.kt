package blazern.lexisoup.data.lexical_item_details_source.tatoeba.api

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
internal data class PagingWrapper(
    @SerialName("Sentences")
    val sentences: SentencePaging
)
