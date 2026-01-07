package blazern.lexisoup.test_utils

import blazern.lexisoup.data.lexisoup.graphql.LexisoupApolloClientHolder
import com.apollographql.apollo.ApolloClient
import com.apollographql.apollo.api.ApolloResponse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeApolloClientHolder : LexisoupApolloClientHolder {
    private val transport = FakeNetworkTransport()
    private val apolloClient = ApolloClient.Builder()
        .networkTransport(transport)
        .build()

    override val client: Flow<ApolloClient> = flowOf(apolloClient)

    fun setResponses(vararg responses: ApolloResponse<*>) =
        transport.setResponses(*responses)
}
