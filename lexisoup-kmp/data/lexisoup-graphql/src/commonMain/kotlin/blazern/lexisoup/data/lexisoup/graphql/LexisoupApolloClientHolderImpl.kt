package blazern.lexisoup.data.lexisoup.graphql

import blazern.lexisoup.core.ktor.KtorClientHolder
import blazern.lexisoup.domain.backend_address.BackendAddressProvider
import com.apollographql.apollo.ApolloClient
import com.apollographql.ktor.http.KtorHttpEngine
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class LexisoupApolloClientHolderImpl(
    ktorClientHolder: KtorClientHolder,
    backendAddressProvider: BackendAddressProvider,
) : LexisoupApolloClientHolder {
    override val client: Flow<ApolloClient> = backendAddressProvider.baseUrl.map { baseUrl ->
        ApolloClient.Builder()
            .serverUrl("$baseUrl/graphql")
            .httpEngine(KtorHttpEngine(ktorClientHolder.client))
            .build()
    }
}
