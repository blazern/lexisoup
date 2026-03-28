package blazern.lexisoup.data.suggestions.di

import blazern.lexisoup.data.suggestions.SuggestionsProvider
import blazern.lexisoup.data.suggestions.SuggestionsProviderImpl
import org.koin.dsl.module

fun suggestionsModule() = module {
    single<SuggestionsProvider> {
        SuggestionsProviderImpl(
            apolloClientHolder = get(),
        )
    }
}
