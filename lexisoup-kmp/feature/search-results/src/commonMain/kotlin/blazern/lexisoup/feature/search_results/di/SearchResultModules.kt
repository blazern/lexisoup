package blazern.lexisoup.feature.search_results.di

import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.feature.search_results.ui.SearchResultsViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun searchResultModules() = listOf(
    module {
        viewModel { (query: String, langFrom: Lang, langTo: Lang) ->
            SearchResultsViewModel(
                query = query,
                langFrom = langFrom,
                langTo = langTo,
                dataSource = get(),
            )
        }
    }
)
