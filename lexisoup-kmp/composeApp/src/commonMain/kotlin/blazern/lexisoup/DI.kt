package blazern.lexisoup

import blazern.lexisoup.core.ktor.di.ktorModule
import blazern.lexisoup.data.lexical_item_details_source.aggregation.di.aggregatingLexicalItemDetailsSourceModules
import blazern.lexisoup.data.lexisoup.graphql.di.lexisoupGraphQLModule
import blazern.lexisoup.data.suggestions.di.suggestionsModule
import blazern.lexisoup.data.translator.aggregation.di.translatorsAggregatorModule
import blazern.lexisoup.domain.backend_address.di.backendAddressModule
import blazern.lexisoup.domain.config.impl.di.configModule
import blazern.lexisoup.domain.settings.di.settingsModule
import blazern.lexisoup.feature.home.di.homeScreenModule
import blazern.lexisoup.feature.search_results.di.searchResultModules
import blazern.lexisoup.feature.settings.di.settingsModules
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.option.viewModelScopeFactory

fun initKoin() {
    startKoin {
        options(
            viewModelScopeFactory()
        )
        modules(
            platformModule(),
            ktorModule(),
            configModule(),
            settingsModule(),
            backendAddressModule(),
            homeScreenModule(),
            lexisoupGraphQLModule(),
            suggestionsModule(),
            translatorsAggregatorModule(),
            *searchResultModules().toTypedArray(),
            *aggregatingLexicalItemDetailsSourceModules().toTypedArray(),
            *settingsModules().toTypedArray(),
        )
    }
}

internal expect fun platformModule(): Module
