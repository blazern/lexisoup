package blazern.lexisoup.data.translator.aggregation

import blazern.lexisoup.data.translator.api.Translator
import blazern.lexisoup.domain.model.DataSource

interface TranslatorsAggregator {
    suspend fun dataSources(): List<DataSource>
    suspend fun getTranslator(dataSource: DataSource): Translator
}
