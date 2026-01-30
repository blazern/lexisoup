package blazern.lexisoup.domain.model

import androidx.compose.runtime.Composable
import blazern.lexisoup.core.ui.strings.stringResource
import blazern.lexisoup.domain.model.DataSource.ChatGPT
import blazern.lexisoup.domain.model.DataSource.Kaikki
import blazern.lexisoup.domain.model.DataSource.PanLex
import blazern.lexisoup.domain.model.DataSource.Tatoeba
import blazern.lexisoup.domain.model.DataSource.WortschatzLeipzig
import lexisoup.core.ui.strings.generated.resources.Res
import lexisoup.core.ui.strings.generated.resources.general_data_source_chatgpt
import lexisoup.core.ui.strings.generated.resources.general_data_source_kaikki
import lexisoup.core.ui.strings.generated.resources.general_data_source_panlex
import lexisoup.core.ui.strings.generated.resources.general_data_source_tatoeba
import lexisoup.core.ui.strings.generated.resources.general_data_source_wortschatz_leipzig

sealed class DataSource(open val id: String) {
    object Tatoeba : DataSource("tatoeba")
    object ChatGPT : DataSource("chatgpt")
    object Kaikki : DataSource("kaikki")
    object PanLex : DataSource("panlex")
    object WortschatzLeipzig : DataSource("wortschatz_leipzig")
    data class Other(override val id: String) : DataSource(id)

    companion object
}

val DataSource.Companion.predefined: List<DataSource>
    get() = listOf(
        Tatoeba,
        ChatGPT,
        Kaikki,
        PanLex,
        WortschatzLeipzig
    )

@Composable
fun DataSource.i18n(): String = when (this) {
    DataSource.Tatoeba ->
        stringResource(Res.string.general_data_source_tatoeba, preview = id)
    DataSource.ChatGPT ->
        stringResource(Res.string.general_data_source_chatgpt, preview = id)
    DataSource.Kaikki ->
        stringResource(Res.string.general_data_source_kaikki, preview = id)
    DataSource.PanLex ->
        stringResource(Res.string.general_data_source_panlex, preview = id)
    DataSource.WortschatzLeipzig ->
        stringResource(Res.string.general_data_source_wortschatz_leipzig, preview = id)
    is DataSource.Other -> id
}
