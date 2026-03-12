package blazern.lexisoup.domain.model

import androidx.compose.runtime.Composable
import blazern.lexisoup.core.ui.strings.stringResource
import blazern.lexisoup.domain.model.DataSource.ChatGPT
import blazern.lexisoup.domain.model.DataSource.DeepL
import blazern.lexisoup.domain.model.DataSource.Kaikki
import blazern.lexisoup.domain.model.DataSource.PanLex
import blazern.lexisoup.domain.model.DataSource.Tatoeba
import blazern.lexisoup.domain.model.DataSource.WortschatzLeipzig
import lexisoup.core.ui.strings.generated.resources.Res
import lexisoup.core.ui.strings.generated.resources.general_data_source_chatgpt
import lexisoup.core.ui.strings.generated.resources.general_data_source_deepl
import lexisoup.core.ui.strings.generated.resources.general_data_source_kaikki
import lexisoup.core.ui.strings.generated.resources.general_data_source_mlkit
import lexisoup.core.ui.strings.generated.resources.general_data_source_panlex
import lexisoup.core.ui.strings.generated.resources.general_data_source_tatoeba
import lexisoup.core.ui.strings.generated.resources.general_data_source_wortschatz_leipzig

sealed class DataSource(open val id: String) {
    object Tatoeba : DataSource("tatoeba")
    object ChatGPT : DataSource("chatgpt")
    object Kaikki : DataSource("kaikki")
    object PanLex : DataSource("panlex")
    object WortschatzLeipzig : DataSource("wortschatz_leipzig")
    object DeepL : DataSource("deepl")
    object MlKit : DataSource("mlkit")

    data class Other(override val id: String) : DataSource(id)

    /**
     * @property impl the real data source which the backend has under the hood. `null` if unknown.
     */
    data class Backend(val impl: DataSource?) : DataSource("backend")

    override fun toString() = id

    companion object
}

val DataSource.Companion.predefined: List<DataSource>
    get() = listOf(
        Tatoeba,
        ChatGPT,
        Kaikki,
        PanLex,
        WortschatzLeipzig,
        DeepL,
        DataSource.MlKit
    )

@Composable
fun DataSource.i18n(): String = when (this) {
    Tatoeba ->
        stringResource(Res.string.general_data_source_tatoeba, preview = id)
    ChatGPT ->
        stringResource(Res.string.general_data_source_chatgpt, preview = id)
    Kaikki ->
        stringResource(Res.string.general_data_source_kaikki, preview = id)
    PanLex ->
        stringResource(Res.string.general_data_source_panlex, preview = id)
    WortschatzLeipzig ->
        stringResource(Res.string.general_data_source_wortschatz_leipzig, preview = id)
    DeepL ->
        stringResource(Res.string.general_data_source_deepl, preview = id)
    DataSource.MlKit ->
        stringResource(Res.string.general_data_source_mlkit, preview = id)
    is DataSource.Backend -> when {
        impl != null -> impl.i18n()
        else -> id
    }
    is DataSource.Other -> id
}
