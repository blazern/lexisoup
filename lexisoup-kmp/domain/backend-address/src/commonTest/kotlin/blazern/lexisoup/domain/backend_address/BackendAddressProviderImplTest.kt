package blazern.lexisoup.domain.backend_address

import blazern.lexisoup.domain.config.api.ConfigProvider
import blazern.lexisoup.domain.config.api.model.Config
import blazern.lexisoup.domain.settings.SettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BackendAddressProviderImplTest {
    private val settings = FakeSettings()
    private val configProvider = FakeConfigProvider()

    private val backendAddressProvider = BackendAddressProviderImpl(
        settings,
        configProvider,
    )

    @Test
    fun `default backend address by default`() = runTest {
        assertEquals(DEFAULT_BASE_URL, backendAddressProvider.baseUrl.first())
        assertFalse(backendAddressProvider.isLocalhost.first())
    }

    @Test
    fun `localhost can be set and reset`() = runTest {
        backendAddressProvider.setIsLocalhost(true)
        assertEquals(LOCALHOST_BASE_URL, backendAddressProvider.baseUrl.first())
        assertTrue(backendAddressProvider.isLocalhost.first())

        backendAddressProvider.setIsLocalhost(false)
        assertEquals(DEFAULT_BASE_URL, backendAddressProvider.baseUrl.first())
        assertFalse(backendAddressProvider.isLocalhost.first())
    }

    @Test
    fun `can override default address`() = runTest {
        val otherUrl = "https://example.com"
        configProvider.configState.update {
            it.copy(backendRedirectionUrl = otherUrl)
        }

        assertEquals(otherUrl, backendAddressProvider.baseUrl.first())
        assertFalse(backendAddressProvider.isLocalhost.first())
    }

    @Test
    fun `cannot override localhost address`() = runTest {
        backendAddressProvider.setIsLocalhost(true)

        val otherUrl = "https://example.com"
        configProvider.configState.update {
            it.copy(backendRedirectionUrl = otherUrl)
        }

        assertEquals(LOCALHOST_BASE_URL, backendAddressProvider.baseUrl.first())
        assertTrue(backendAddressProvider.isLocalhost.first())
    }
}

private class FakeSettings : SettingsRepository {
    private val backendBaseUrl = MutableStateFlow<String?>(null)

    override fun getBackendBaseUrl(defaultValue: String) =
        backendBaseUrl.map { it ?: defaultValue }

    override suspend fun setBackendBaseUrl(baseUrl: String) =
        backendBaseUrl.update { baseUrl }
}

private class FakeConfigProvider : ConfigProvider {
    val configState = MutableStateFlow(Config(
        backendRedirectionUrl = null,
        minQueryLength = 123,
        maxQueryLength = 123,
        translatorsParams = emptyMap(),
    ))

    override val config = configState.asSharedFlow()
}
