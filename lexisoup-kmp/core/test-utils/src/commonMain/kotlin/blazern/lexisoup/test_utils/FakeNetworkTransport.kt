package blazern.lexisoup.test_utils

import com.apollographql.apollo.api.ApolloRequest
import com.apollographql.apollo.api.ApolloResponse
import com.apollographql.apollo.api.Operation
import com.apollographql.apollo.network.NetworkTransport
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

internal class FakeNetworkTransport : NetworkTransport {
    private val responses = mutableListOf<ApolloResponse<*>>()

    fun setResponses(vararg responses: ApolloResponse<*>) {
        this.responses.clear()
        this.responses.addAll(responses.toMutableList())
    }

    @Suppress("UNCHECKED_CAST")
    override fun <D : Operation.Data> execute(
        request: ApolloRequest<D>
    ): Flow<ApolloResponse<D>> = flow {
        val response = responses.removeFirstOrNull()
            ?: error("No response configured in TestNetworkTransport for request: ${request.operation.name()}")
        emit(response as ApolloResponse<D>)
    }

    override fun dispose() {
        responses.clear()
    }
}
