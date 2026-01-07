package blazern.lexisoup.feature.home.di

import blazern.lexisoup.feature.home.ui.HomeScreenViewModel
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

fun homeScreenModule() = module {
    viewModel { (query: String) ->
        HomeScreenViewModel(
            query = query,
            settings = get(),
            backendAddressProvider = get(),
            suggestionsProvider = get(),
        )
    }
}
