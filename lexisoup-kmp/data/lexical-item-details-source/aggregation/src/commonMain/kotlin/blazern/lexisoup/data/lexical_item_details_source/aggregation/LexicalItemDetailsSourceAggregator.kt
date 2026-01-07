package blazern.lexisoup.data.lexical_item_details_source.aggregation

import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools.FormsAccentsEnhancerProvider

class LexicalItemDetailsSourceAggregator(
    private val dataSources: List<LexicalItemDetailsSource>,
    private val accentsEnhancerProvider: FormsAccentsEnhancerProvider,
) {
    fun sourceFor(
        query: String,
        langFrom: Lang,
        langTo: Lang,
    ) = LexicalItemDetailsSourceAggregatorForQuery(
        query,
        langFrom,
        langTo,
        accentsEnhancerProvider,
        dataSources,
    )
}
