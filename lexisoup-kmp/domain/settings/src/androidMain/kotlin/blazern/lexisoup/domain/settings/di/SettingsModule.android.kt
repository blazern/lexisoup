package blazern.lexisoup.domain.settings.di

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.core.scope.Scope

internal actual fun Scope.observableSettings(): ObservableSettings {
    return SharedPreferencesSettings.Factory(
        context = get(),
    ).create(name = "main")
}
