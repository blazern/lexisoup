package blazern.lexisoup.domain.settings

import blazern.lexisoup.domain.model.Lang
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

    override suspend fun getTatoebaAcceptableTagsSets(): List<Set<String>> {
        return listOf(
            setOf("nominative", "singular"),
            setOf("nominative", "plural"),
        )
    }

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

    private companion object {
        const val KEY_LANG_FROM = "lang_from"
        const val KEY_LANG_TO = "lang_to"
        const val KEY_BACKEND_BASE_URL = "backend_base_url"
    }
}
