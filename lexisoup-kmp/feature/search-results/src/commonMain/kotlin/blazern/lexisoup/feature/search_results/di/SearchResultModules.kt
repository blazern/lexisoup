package blazern.lexisoup.feature.search_results.di

import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.feature.search_results.ui.SearchResultsViewModel
import blazern.lexisoup.feature.search_results.usecases.CanTranslateUseCase
import blazern.lexisoup.feature.search_results.usecases.CreateTranslationsStatesUseCase
import blazern.lexisoup.feature.search_results.usecases.CreateTranslationsStatesUseCaseImpl
import blazern.lexisoup.feature.search_results.usecases.TransformPageUseCase
import blazern.lexisoup.feature.search_results.usecases.TranslateDetailsUseCase
import blazern.lexisoup.feature.search_results.usecases.TranslateDetailsUseCaseImpl
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
                translators = get(),
                translateDetails = get(),
                transformPage = get(),
                createTranslationsStates = get(),
            )
        }

        factory { CanTranslateUseCase() }

        factory { TransformPageUseCase() }

        factory<TranslateDetailsUseCase> {
            TranslateDetailsUseCaseImpl(
                canTranslate = get(),
            )
        }

        factory<CreateTranslationsStatesUseCase> {
            CreateTranslationsStatesUseCaseImpl(
                translators = get(),
                canTranslate = get(),
            )
        }
    }
)
