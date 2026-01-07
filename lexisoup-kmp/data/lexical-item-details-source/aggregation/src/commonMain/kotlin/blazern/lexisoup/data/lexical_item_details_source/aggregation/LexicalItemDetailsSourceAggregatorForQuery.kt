package blazern.lexisoup.data.lexical_item_details_source.aggregation

import arrow.core.Either
import arrow.core.getOrElse
import blazern.lexisoup.core.logging.Log
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource.Item
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.copy
import blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools.FormsAccentsEnhancer
import blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools.FormsAccentsEnhancerProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class LexicalItemDetailsSourceAggregatorForQuery internal constructor(
    private val query: String,
    private val langFrom: Lang,
    private val langTo: Lang,
    private val accentsEnhancerProvider: FormsAccentsEnhancerProvider,
    dataSources: List<LexicalItemDetailsSource>,
) {
    private val dataSources = dataSources.associateBy { it.source }

    private var accentsEnhancer: Either<Err, FormsAccentsEnhancer>? = null
    private val accentsEnhancerMutex = Mutex()

    fun typesOf(source: DataSource): Set<LexicalItemDetail.Type> =
        dataSources[source]?.types.orEmpty()

    fun request(source: DataSource): Flow<Item> {
        val flow = dataSources[source]?.request(query, langFrom, langTo) ?: emptyFlow()
        return flow.map {
            when (it) {
                is Item.Page -> enhance(it)
                else -> it
            }
        }
    }

    private suspend fun enhance(page: Item.Page): Item.Page {
        return page.copy(
            details = page.details.map { enhance(it) }
        )
    }

    private suspend fun enhance(detail: LexicalItemDetail): LexicalItemDetail {
        val accentsEnhancer = accentsEnhancerMutex.withLock {
            accentsEnhancer ?: accentsEnhancerProvider.provideFor(query, langFrom, langTo).also {
                accentsEnhancer = it
                it.onLeft {
                    Log.e(TAG, it.e) { "Could not get FormsAccentsEnhancer" }
                }
            }
        }.getOrElse {
            return detail
        }

        return when (detail) {
            is LexicalItemDetail.Example -> detail.copy(
                translationsSet = detail.translationsSet.copy(
                    original = accentsEnhancer.enhance(detail.translationsSet.original)
                )
            )
            else -> detail
        }
    }

    private companion object {
        const val TAG = "LexicalItemDetailsSourceAggregatorForQuery"
    }
}
