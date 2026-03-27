package blazern.lexisoup.data.lexical_item_details_source.aggregation.di

import blazern.lexisoup.data.kaikki.di.kaikkiModule
import blazern.lexisoup.data.lexical_item_details_source.aggregation.LexicalItemDetailsSourceAggregator
import blazern.lexisoup.data.lexical_item_details_source.api.LexicalItemDetailsSource
import blazern.lexisoup.data.lexical_item_details_source.kaikki.KaikkiLexicalItemDetailsSource
import blazern.lexisoup.data.lexical_item_details_source.kaikki.KaikkiLexicalItemDetailsSourceImpl
import blazern.lexisoup.data.lexical_item_details_source.panlex.PanLexLexicalItemDetailsSource
import blazern.lexisoup.data.lexical_item_details_source.tatoeba.TatoebaLexicalItemDetailsSource
import blazern.lexisoup.data.lexical_item_details_source.utils.cache.LexicalItemDetailsSourceCacher
import blazern.lexisoup.data.lexical_item_details_source.utils.examples_tools.di.examplesToolsModule
import blazern.lexisoup.data.lexical_item_details_source.wortschatz_leipzig.WortschatzLeipzigLexicalItemDetailsSource
import blazern.lexisoup.model.lexical_item_details_source.chatgpt.ChatGPTLexicalItemDetailsSource
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import org.koin.dsl.bind
import org.koin.dsl.module

fun aggregatingLexicalItemDetailsSourceModules() = listOf(
    kaikkiModule(),
    examplesToolsModule(),
    module {
        single {
            // [GlobalScope] for a singleton.
            @OptIn(DelicateCoroutinesApi::class)
            LexicalItemDetailsSourceCacher(GlobalScope)
        }

        single {
            TatoebaLexicalItemDetailsSource(
                ktorClientHolder = get(),
                backendAddressProvider = get(),
                formsForExamplesProvider = get(),
            )
        }.bind(LexicalItemDetailsSource::class)

        single {
            ChatGPTLexicalItemDetailsSource(
                apolloClientHolder = get(),
                cacher = get(),
            )
        }.bind(LexicalItemDetailsSource::class)

        single<KaikkiLexicalItemDetailsSource> {
            KaikkiLexicalItemDetailsSourceImpl(
                kaikkiClient = get(),
                cacher = get(),
            )
        }.bind(LexicalItemDetailsSource::class)

        single {
            PanLexLexicalItemDetailsSource(
                apolloClientHolder = get(),
                cacher = get(),
            )
        }.bind(LexicalItemDetailsSource::class)

        single {
            WortschatzLeipzigLexicalItemDetailsSource(
                backendAddressProvider = get(),
                ktorClientHolder = get(),
                formsForExamplesProvider = get(),
            )
        }.bind(LexicalItemDetailsSource::class)

        single {
            LexicalItemDetailsSourceAggregator(
                dataSources = getAll(),
                accentsEnhancerProvider = get(),
            )
        }
    }
)
