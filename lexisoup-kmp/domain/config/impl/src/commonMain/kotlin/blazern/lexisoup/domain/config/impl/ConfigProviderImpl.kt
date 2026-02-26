package blazern.lexisoup.domain.config.impl

import blazern.lexisoup.core.logging.Log
import blazern.lexisoup.data.lexisoup.graphql.LexisoupApolloClientHolder
import blazern.lexisoup.data.lexisoup.graphql.err
import blazern.lexisoup.domain.config.api.ConfigProvider
import blazern.lexisoup.domain.config.api.model.Config
import blazern.lexisoup.graphql.model.ClientConfigQuery
import com.apollographql.apollo.ApolloClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@OptIn(DelicateCoroutinesApi::class)
class ConfigProviderImpl(
    private val apolloClientHolder: Lazy<LexisoupApolloClientHolder>,
    private val coroutineScope: CoroutineScope = GlobalScope,
) : ConfigProvider {
    private val apollo: Flow<ApolloClient>
        get() = apolloClientHolder.value.client

    override val config = MutableStateFlow(
        Config(
            backendRedirectionUrl = null,
            minQueryLength = 3,
            maxQueryLength = 50,
            translateTextLengthMax = 50,
            translateTextLengthMin = 3,
            translateBatchSizeLimit = 10,
        )
    )

    init {
        coroutineScope.launch {
            val result = apollo.first().query(
                ClientConfigQuery()
            ).execute()

            result.err?.let {
                Log.w(TAG, it.e) { "Error while retrieving remote config" }
                return@launch
            }

            val data = result.data
            if (data == null) {
                Log.e(TAG) { "Empty remote config received" }
                return@launch
            }

            // NOTE: This config is here only for the sake of it, but an
            // appropriate implementation would also store it persistently
            config.update {
                Config(
                    backendRedirectionUrl = data.config.backendRedirectionUrl,
                    minQueryLength = data.config.minQueryLength,
                    maxQueryLength = data.config.maxQueryLength,
                    translateTextLengthMax = data.config.translateTextLengthMax,
                    translateTextLengthMin = data.config.translateTextLengthMin,
                    translateBatchSizeLimit = data.config.translateBatchSizeLimit,
                )
            }
            Log.i(TAG) { "Received config: ${config.value}" }
        }
    }
}

private const val TAG = "ConfigProviderImpl"