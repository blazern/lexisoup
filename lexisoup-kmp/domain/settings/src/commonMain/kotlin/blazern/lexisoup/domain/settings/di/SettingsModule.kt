package blazern.lexisoup.domain.settings.di

import blazern.lexisoup.domain.settings.SettingsRepository
import blazern.lexisoup.domain.settings.SettingsRepositoryImpl
import com.russhwolf.settings.ObservableSettings
import org.koin.core.scope.Scope
import org.koin.dsl.module

fun settingsModule() = module {
    single<SettingsRepository> {
        SettingsRepositoryImpl(
            settings = get()
        )
    }
    single {
        observableSettings()
    }
}

internal expect fun Scope.observableSettings(): ObservableSettings
