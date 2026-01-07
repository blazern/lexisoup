package blazern.lexisoup.data.lexisoup.graphql.di

import blazern.lexisoup.data.lexisoup.graphql.LexisoupApolloClientHolder
import blazern.lexisoup.data.lexisoup.graphql.LexisoupApolloClientHolderImpl
import org.koin.dsl.module

fun lexisoupGraphQLModule() = module {
    single<LexisoupApolloClientHolder> {
        LexisoupApolloClientHolderImpl(
            ktorClientHolder = get(),
            backendAddressProvider = get(),
        )
    }
}
