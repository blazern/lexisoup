package blazern.lexisoup.domain.settings

import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface SettingsRepository {
    suspend fun setLangFrom(lang: Lang) = Unit
    fun getLangFrom(): Flow<Lang> = emptyFlow()
    suspend fun setLangTo(lang: Lang) = Unit
    fun getLangTo(): Flow<Lang> = emptyFlow()

    suspend fun setBackendBaseUrl(baseUrl: String) = Unit
    fun getBackendBaseUrl(defaultValue: String): Flow<String> = emptyFlow()

    suspend fun setExcludedDataSourcesIDs(ids: Set<String>) = Unit
    fun getExcludedDataSourcesIDs(): Flow<Set<String>> = emptyFlow()

    suspend fun setExcludedLexicalItemsDetailsTypes(types: Set<LexicalItemDetail.Type>) = Unit
    fun getExcludedLexicalItemsDetailsTypes(): Flow<Set<LexicalItemDetail.Type>> = emptyFlow()
}
