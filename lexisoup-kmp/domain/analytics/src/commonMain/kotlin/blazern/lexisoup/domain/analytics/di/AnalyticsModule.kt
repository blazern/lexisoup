package blazern.lexisoup.domain.analytics.di

import blazern.lexisoup.domain.analytics.Analytics
import blazern.lexisoup.domain.analytics.createAnalytics
import org.koin.dsl.module

fun analyticsModule() = module {
    single<Analytics> {
        createAnalytics()
    }
}
