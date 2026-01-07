package blazern.lexisoup.data.kaikki.di

import blazern.lexisoup.data.kaikki.KaikkiClient
import blazern.lexisoup.data.kaikki.KaikkiClientImpl
import org.koin.dsl.module

fun kaikkiModule() = module {
    single<KaikkiClient> {
        KaikkiClientImpl(
            ktorClientHolder = get(),
            backendAddressProvider = get(),
        )
    }
}
