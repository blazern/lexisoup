package blazern.lexisoup.domain.backend_address.di

import blazern.lexisoup.domain.backend_address.BackendAddressProvider
import blazern.lexisoup.domain.backend_address.BackendAddressProviderImpl
import org.koin.dsl.module

fun backendAddressModule() = module {
    single<BackendAddressProvider> {
        BackendAddressProviderImpl(
            settings = get(),
        )
    }
}
