package blazern.lexisoup.domain.settings

import blazern.lexisoup.domain.model.Lang
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface SettingsRepository {
    suspend fun getTatoebaAcceptableTagsSets(): List<Set<String>> = emptyList()

    suspend fun setLangFrom(lang: Lang) = Unit
    fun getLangFrom(): Flow<Lang> = emptyFlow()
    suspend fun setLangTo(lang: Lang) = Unit
    fun getLangTo(): Flow<Lang> = emptyFlow()

    suspend fun setBackendBaseUrl(baseUrl: String) = Unit
    fun getBackendBaseUrl(defaultValue: String): Flow<String> = emptyFlow()
}
