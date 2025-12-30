package blazern.lexisoup

import blazern.lexisoup.core.ktor.di.ktorModule
import blazern.lexisoup.data.lexical_item_details_source.aggregation.di.aggregatingLexicalItemDetailsSourceModules
import blazern.lexisoup.data.lexisoup.graphql.di.lexisoupGraphQLModule
import blazern.lexisoup.data.suggestions.di.suggestionsModule
import blazern.lexisoup.domain.backend_address.di.backendAddressModule
import blazern.lexisoup.domain.settings.di.settingsModule
import blazern.lexisoup.feature.home.di.homeScreenModule
import blazern.lexisoup.feature.search_results.di.searchResultModules
import org.koin.core.context.startKoin
import org.koin.core.module.Module

fun initKoin() {
    startKoin {
        modules(
            platformModule(),
            ktorModule(),
            settingsModule(),
            backendAddressModule(),
            homeScreenModule(),
            lexisoupGraphQLModule(),
            suggestionsModule(),
            *searchResultModules().toTypedArray(),
            *aggregatingLexicalItemDetailsSourceModules().toTypedArray(),
        )
    }
}

internal expect fun platformModule(): Module
