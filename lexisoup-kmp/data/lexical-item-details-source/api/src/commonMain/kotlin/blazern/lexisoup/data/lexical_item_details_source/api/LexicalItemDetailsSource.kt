package blazern.lexisoup.data.lexical_item_details_source.api

import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import kotlinx.coroutines.flow.Flow

interface LexicalItemDetailsSource {
    val source: DataSource
    val types: Set<LexicalItemDetail.Type>

    fun request(
        query: String,
        langFrom: Lang,
        langTo: Lang,
    ): Flow<Item>

    sealed interface Item {
        data class Failure(val err: Err) : Item
        data class Page(
            val details: List<LexicalItemDetail>,
            val nextPageTypes: Set<LexicalItemDetail.Type>,
            val errors: List<Err> = emptyList(),
        ) : Item {
            init {
                require(nextPageTypes.isNotEmpty())
            }
        }
    }
}
