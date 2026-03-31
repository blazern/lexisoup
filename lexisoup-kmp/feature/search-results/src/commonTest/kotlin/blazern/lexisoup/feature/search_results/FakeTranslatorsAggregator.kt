package blazern.lexisoup.feature.search_results

import blazern.lexisoup.data.translator.aggregation.TranslatorsAggregator
import blazern.lexisoup.data.translator.api.Translator
import blazern.lexisoup.domain.model.DataSource

class FakeTranslatorsAggregator(
    private val translators: List<Translator>,
) : TranslatorsAggregator {
    override suspend fun dataSources(): List<DataSource> =
        translators.map { it.source }
    override suspend fun getTranslator(dataSource: DataSource) =
        translators.first { it.source == dataSource }
}
