package blazern.lexisoup.data.translator.mlkit

import blazern.lexisoup.core.logging.Log
import blazern.lexisoup.domain.model.Lang
import com.google.mlkit.nl.translate.TranslateLanguage

internal fun Lang.toMlKit(): String = when (this) {
    Lang.RU -> TranslateLanguage.RUSSIAN
    Lang.EN -> TranslateLanguage.ENGLISH
    Lang.DE -> TranslateLanguage.GERMAN
    Lang.FR -> TranslateLanguage.FRENCH
}

internal fun Lang.Companion.fromMlKit(langCode: String): Lang? {
    return when (langCode) {
        TranslateLanguage.RUSSIAN -> Lang.RU
        TranslateLanguage.ENGLISH -> Lang.EN
        TranslateLanguage.GERMAN -> Lang.DE
        TranslateLanguage.FRENCH -> Lang.FR
        else -> {
            Log.e(TAG) { "Please add support for $langCode" }
            null
        }
    }
}

private const val TAG = "LangExtensions"
