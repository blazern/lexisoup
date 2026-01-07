package blazern.lexisoup.domain.model

import lexisoup.core.ui.strings.generated.resources.Res
import lexisoup.core.ui.strings.generated.resources.general_data_source_chatgpt
import lexisoup.core.ui.strings.generated.resources.general_data_source_kaikki
import lexisoup.core.ui.strings.generated.resources.general_data_source_panlex
import lexisoup.core.ui.strings.generated.resources.general_data_source_tatoeba
import lexisoup.core.ui.strings.generated.resources.general_data_source_unknown
import lexisoup.core.ui.strings.generated.resources.general_data_source_wortschatz_leipzig
import org.jetbrains.compose.resources.StringResource

enum class DataSource {
    TATOEBA,
    CHATGPT,
    KAIKKI,
    PANLEX,
    WORTSCHATZ_LEIPZIG,
    UNKNOWN,
}

val DataSource.strRsc: StringResource
    get() {
        return when (this) {
            DataSource.TATOEBA -> Res.string.general_data_source_tatoeba
            DataSource.CHATGPT -> Res.string.general_data_source_chatgpt
            DataSource.KAIKKI -> Res.string.general_data_source_kaikki
            DataSource.PANLEX -> Res.string.general_data_source_panlex
            DataSource.WORTSCHATZ_LEIPZIG -> Res.string.general_data_source_wortschatz_leipzig
            DataSource.UNKNOWN -> Res.string.general_data_source_unknown
        }
    }
