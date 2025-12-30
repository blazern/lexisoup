package blazern.lexisoup.data.suggestions.di

import blazern.lexisoup.data.suggestions.SuggestionsProvider
import org.koin.dsl.module

fun suggestionsModule() = module {
    single {
        SuggestionsProvider(
            apolloClientHolder = get(),
        )
    }
}
