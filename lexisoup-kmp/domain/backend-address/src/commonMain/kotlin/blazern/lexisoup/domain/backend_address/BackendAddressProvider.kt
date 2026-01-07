package blazern.lexisoup.domain.backend_address

import kotlinx.coroutines.flow.Flow

interface BackendAddressProvider{
    val baseUrl: Flow<String>
    val isLocalhost: Flow<Boolean>
    suspend fun setIsLocalhost(isLocalhost: Boolean)
}
