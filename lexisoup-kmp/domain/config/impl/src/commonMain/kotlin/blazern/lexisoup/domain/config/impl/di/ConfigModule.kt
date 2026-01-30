package blazern.lexisoup.domain.config.impl.di

import blazern.lexisoup.domain.config.api.ConfigProvider
import blazern.lexisoup.domain.config.impl.ConfigProviderImpl
import org.koin.dsl.module

fun configModule() = module {
    single<ConfigProvider> {
        ConfigProviderImpl(
            apolloClientHolder = lazy { get() },
        )
    }
}
