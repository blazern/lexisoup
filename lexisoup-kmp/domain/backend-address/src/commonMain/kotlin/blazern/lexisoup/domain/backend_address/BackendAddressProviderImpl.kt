package blazern.lexisoup.domain.backend_address

import blazern.lexisoup.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class BackendAddressProviderImpl(
    private val settings: SettingsRepository,
) : BackendAddressProvider {
    override val baseUrl: Flow<String> = settings.getBackendBaseUrl(
        defaultValue = DEFAULT_BASE_URL,
    )

    override val isLocalhost: Flow<Boolean> = baseUrl.map { it == LOCALHOST_BASE_URL }

    override suspend fun setIsLocalhost(isLocalhost: Boolean) {
        if (isLocalhost) {
            settings.setBackendBaseUrl(LOCALHOST_BASE_URL)
        } else {
            settings.setBackendBaseUrl(DEFAULT_BASE_URL)
        }
    }
}

private const val DEFAULT_BASE_URL = "https://lexisoup.com/api/"
private const val LOCALHOST_BASE_URL = "http://localhost:8888"
