package blazern.lexisoup.domain.settings.di

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.ObservableSettings
import org.koin.core.scope.Scope

internal actual fun Scope.observableSettings(): ObservableSettings {
    return NSUserDefaultsSettings.Factory().create(name = "main")
}
