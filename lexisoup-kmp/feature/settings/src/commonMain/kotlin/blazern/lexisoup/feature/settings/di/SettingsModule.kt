package blazern.lexisoup.feature.settings.di

import blazern.lexisoup.feature.settings.ui.SettingsViewModel
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.core.annotation.KoinViewModelScopeApi
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module
import org.koin.viewmodel.scope.viewModelScope

@OptIn(KoinExperimentalAPI::class, KoinViewModelScopeApi::class)
fun settingsModules() = listOf(
    module {
        viewModelScope {
            viewModel {
                SettingsViewModel()
            }
        }
    }
)
