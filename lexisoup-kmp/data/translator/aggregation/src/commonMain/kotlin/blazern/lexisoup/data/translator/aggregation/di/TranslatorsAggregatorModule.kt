package blazern.lexisoup.data.translator.aggregation.di

import blazern.lexisoup.data.translator.aggregation.TranslatorsAggregator
import blazern.lexisoup.data.translator.aggregation.TranslatorsAggregatorImpl
import blazern.lexisoup.data.translator.api.Translator
import blazern.lexisoup.data.translator.backend.BackendTranslator
import org.koin.dsl.bind
import org.koin.dsl.module

fun translatorsAggregatorModule() = module {
    single {
        BackendTranslator(
            configProvider = get(),
            apolloClientHolder = get(),
        )
    }.bind(Translator::class)

    single<TranslatorsAggregator> {
        TranslatorsAggregatorImpl(
            translators = getAll(),
        )
    }
}
