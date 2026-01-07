package blazern.lexisoup.core.ktor.di

import blazern.lexisoup.core.ktor.KtorClientHolder
import org.koin.dsl.module

fun ktorModule() = module {
    single {
        KtorClientHolder()
    }
}