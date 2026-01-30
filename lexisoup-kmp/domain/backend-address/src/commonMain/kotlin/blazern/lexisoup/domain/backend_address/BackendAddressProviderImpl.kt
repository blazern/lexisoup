package blazern.lexisoup.domain.backend_address

import blazern.lexisoup.domain.config.api.ConfigProvider
import blazern.lexisoup.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map

internal class BackendAddressProviderImpl(
    private val settings: SettingsRepository,
    configProvider: ConfigProvider,
) : BackendAddressProvider {
    override val baseUrl: Flow<String> = combine(
        settings.getBackendBaseUrl(DEFAULT_BASE_URL),
        configProvider.config,
    ) { settingsUrl, config ->
        val backendRedirectionUrl = config.backendRedirectionUrl
        when (settingsUrl) {
            DEFAULT_BASE_URL -> backendRedirectionUrl ?: settingsUrl
            else -> settingsUrl
        }
    }

    override val isLocalhost: Flow<Boolean> = baseUrl.map { it == LOCALHOST_BASE_URL }

    override suspend fun setIsLocalhost(isLocalhost: Boolean) {
        if (isLocalhost) {
            settings.setBackendBaseUrl(LOCALHOST_BASE_URL)
        } else {
            settings.setBackendBaseUrl(DEFAULT_BASE_URL)
        }
    }
}

internal const val DEFAULT_BASE_URL = "https://lexisoup.com/api/"
internal const val LOCALHOST_BASE_URL = "http://localhost:8888"
