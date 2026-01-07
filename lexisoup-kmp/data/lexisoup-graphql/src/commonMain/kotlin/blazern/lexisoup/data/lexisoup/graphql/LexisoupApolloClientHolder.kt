package blazern.lexisoup.data.lexisoup.graphql

import com.apollographql.apollo.ApolloClient
import kotlinx.coroutines.flow.Flow

interface LexisoupApolloClientHolder {
    val client: Flow<ApolloClient>
}
