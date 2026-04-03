package blazern.lexisoup.feature.search_results.di

import androidx.lifecycle.viewModelScope
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.feature.search_results.repository.AudiosPlaybackRepository
import blazern.lexisoup.feature.search_results.repository.AudiosPlaybackRepositoryImpl
import blazern.lexisoup.feature.search_results.repository.BackgroundErrorsRepository
import blazern.lexisoup.feature.search_results.ui.SearchResultsViewModel
import blazern.lexisoup.feature.search_results.usecases.CanTranslateUseCase
import blazern.lexisoup.feature.search_results.usecases.CreateTranslationsStatesUseCase
import blazern.lexisoup.feature.search_results.usecases.CreateTranslationsStatesUseCaseImpl
import blazern.lexisoup.feature.search_results.usecases.TransformPageUseCase
import blazern.lexisoup.feature.search_results.usecases.TranslateDetailsUseCase
import blazern.lexisoup.feature.search_results.usecases.TranslateDetailsUseCaseImpl
import kotlinx.coroutines.CoroutineScope
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.annotation.KoinViewModelScopeApi
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.viewmodel.scope.viewModelScope

@OptIn(KoinExperimentalAPI::class, KoinViewModelScopeApi::class)
fun searchResultModules() = listOf(
    module {
        viewModelScope {
            viewModel { (query: String, langFrom: Lang, langTo: Lang) ->
                SearchResultsViewModel(
                    query = query,
                    langFrom = langFrom,
                    langTo = langTo,
                    analytics = get(),
                    dataSource = get(),
                    translators = get(),
                    translateDetails = get(),
                    transformPage = get(),
                    createTranslationsStates = get(),
                    errorsRepo = get(),
                )
            }

            scoped<CoroutineScope> {
                getSource<SearchResultsViewModel>()!!.viewModelScope
            }

            scoped {
                BackgroundErrorsRepository()
            }

            scoped<AudiosPlaybackRepository> {
                AudiosPlaybackRepositoryImpl(
                    errorsRepo = get(),
                    scope = get(),
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
    }
)
