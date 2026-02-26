package blazern.lexisoup.data.translator.aggregation

import blazern.lexisoup.data.translator.api.Translator
import blazern.lexisoup.domain.model.DataSource

interface TranslatorsAggregator {
    val dataSources: List<DataSource>
    fun getTranslator(dataSource: DataSource): Translator
}
