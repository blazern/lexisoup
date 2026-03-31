package blazern.lexisoup.data.translator.aggregation

import blazern.lexisoup.data.translator.api.Translator
import blazern.lexisoup.data.translator.backend.BackendTranslator
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.settings.SettingsRepository
import blazern.lexisoup.utils.KotlinPlatform
import blazern.lexisoup.utils.getKotlinPlatform
import kotlinx.coroutines.flow.first

internal class TranslatorsAggregatorImpl(
    translators: List<Translator>,
    private val settings: SettingsRepository,
) : TranslatorsAggregator {
    private val translators = translators
        .associateBy { it.source }

    override suspend fun dataSources(): List<DataSource> {
        val translatorsKeys = this.translators.keys
        val excludedDataSources = settings.getExcludedDataSourcesIDs().first()
        return translatorsKeys.filter { !excludedDataSources.contains(it.id) }
    }

    override suspend fun getTranslator(dataSource: DataSource): Translator =
        translators[dataSource] ?: throw IllegalArgumentException(
            "$dataSource not supported, please use the dataSources function"
        )
}
