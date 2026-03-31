package blazern.lexisoup.domain.settings

import blazern.lexisoup.core.logging.Log
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalSettingsApi::class)
class SettingsRepositoryImpl(
    settings: ObservableSettings,
) : SettingsRepository {
    private val settings: FlowSettings = settings.toFlowSettings()

    override suspend fun setLangFrom(lang: Lang) =
        settings.putString(KEY_LANG_FROM, lang.iso3)

    override fun getLangFrom(): Flow<Lang> =
        settings.getStringFlow(KEY_LANG_FROM, Lang.EN.iso3)
            .map { Lang.fromIso3(it) ?: Lang.EN }

    override suspend fun setLangTo(lang: Lang) =
        settings.putString(KEY_LANG_TO, lang.iso3)

    override fun getLangTo(): Flow<Lang> =
        settings.getStringFlow(KEY_LANG_TO, Lang.DE.iso3)
            .map { Lang.fromIso3(it) ?: Lang.DE }

    override suspend fun setBackendBaseUrl(baseUrl: String) =
        settings.putString(KEY_BACKEND_BASE_URL, baseUrl)

    override fun getBackendBaseUrl(defaultValue: String): Flow<String> =
        settings.getStringFlow(KEY_BACKEND_BASE_URL, defaultValue)

    override suspend fun setExcludedDataSourcesIDs(ids: Set<String>) =
        settings.putString(KEY_EXCLUDED_DATA_SOURCES, ids.joinToString(DELIMITER))

    override fun getExcludedDataSourcesIDs(): Flow<Set<String>> =
        settings.getStringFlow(KEY_EXCLUDED_DATA_SOURCES, "").map {
            it.split(DELIMITER)
                .filter { it.isNotBlank() }
                .toSet()
        }

    override suspend fun setExcludedLexicalItemsDetailsTypes(types: Set<LexicalItemDetail.Type>) =
        settings.putString(KEY_EXCLUDED_DETAILS_TYPES, types.joinToString(DELIMITER) { it.name })

    override fun getExcludedLexicalItemsDetailsTypes(): Flow<Set<LexicalItemDetail.Type>> =
        settings.getStringFlow(
            KEY_EXCLUDED_DETAILS_TYPES,
            defaultValue = setOf(LexicalItemDetail.Type.ETYMOLOGY)
                .joinToString(DELIMITER) { it.name },
        ).map {
            it.split(DELIMITER)
                .mapNotNull { typeStr ->
                    LexicalItemDetail.Type.entries.firstOrNull { it.name == typeStr }.also {
                        if (it == null) {
                            Log.e(TAG) { "Could not find lexical item detail type for $typeStr" }
                        }
                    }
                }.toSet()
        }

    private companion object {
        const val KEY_LANG_FROM = "lang_from"
        const val KEY_LANG_TO = "lang_to"
        const val KEY_BACKEND_BASE_URL = "backend_base_url"
        const val KEY_EXCLUDED_DATA_SOURCES = "excluded_data_sources"
        const val KEY_EXCLUDED_DETAILS_TYPES = "excluded_lexical_items_details_types"

        const val DELIMITER = ";"
        const val TAG = "SettingsRepositoryImpl"
    }
}
