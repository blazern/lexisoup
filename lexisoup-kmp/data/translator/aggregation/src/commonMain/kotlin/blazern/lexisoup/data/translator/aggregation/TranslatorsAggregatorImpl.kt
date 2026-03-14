package blazern.lexisoup.data.translator.aggregation

import blazern.lexisoup.data.translator.api.Translator
import blazern.lexisoup.data.translator.backend.BackendTranslator
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.utils.KotlinPlatform
import blazern.lexisoup.utils.getKotlinPlatform

internal class TranslatorsAggregatorImpl(
    translators: List<Translator>,
) : TranslatorsAggregator {
    private val translators = translators
        .associateBy { it.source }

    override val dataSources: List<DataSource> = this.translators.keys.toList()

    override fun getTranslator(dataSource: DataSource): Translator =
        translators[dataSource] ?: throw IllegalArgumentException(
            "$dataSources not supported, please use the dataSources property"
        )
}
