package blazern.lexisoup.domain.model

import lexisoup.core.ui.strings.generated.resources.Res
import org.jetbrains.compose.resources.StringResource
import lexisoup.core.ui.strings.generated.resources.general_lang_ru
import lexisoup.core.ui.strings.generated.resources.general_lang_en
import lexisoup.core.ui.strings.generated.resources.general_lang_de
import lexisoup.core.ui.strings.generated.resources.general_lang_fr

enum class Lang(
    val iso2: String,
    val iso3: String,
) {
    RU("ru", "rus"),
    EN("en", "eng"),
    DE("de", "deu"),
    FR("fr", "fra"),
    ;

    companion object {
        private val iso2Map = entries.associateBy { it.iso2 }
        private val iso3Map = entries.associateBy { it.iso3 }
        fun fromIso2(iso2: String): Lang? = iso2Map[iso2]
        fun fromIso3(iso3: String): Lang? = iso3Map[iso3]
    }
}

val Lang.strRsc: StringResource
    get() {
        return when (this) {
            Lang.RU -> Res.string.general_lang_ru
            Lang.EN -> Res.string.general_lang_en
            Lang.DE -> Res.string.general_lang_de
            Lang.FR -> Res.string.general_lang_fr
        }
    }
