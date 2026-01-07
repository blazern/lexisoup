package blazern.lexisoup.domain.settings.di

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.StorageSettings
import com.russhwolf.settings.observable.makeObservable
import org.koin.core.scope.Scope

@OptIn(ExperimentalSettingsApi::class)
internal actual fun Scope.observableSettings(): ObservableSettings {
    return StorageSettings().makeObservable()
}
