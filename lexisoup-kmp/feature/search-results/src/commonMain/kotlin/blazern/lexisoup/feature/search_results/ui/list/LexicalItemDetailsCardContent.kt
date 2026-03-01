package blazern.lexisoup.feature.search_results.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import blazern.lexisoup.core.ui.strings.stringResource
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.LexicalItemDetail.Explanation
import blazern.lexisoup.domain.model.LexicalItemDetail.Synonyms
import blazern.lexisoup.domain.model.LexicalItemDetail.WordTranslations
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.feature.search_results.model.LexicalItemDetailsGroupState
import blazern.lexisoup.feature.search_results.model.SearchRequest
import blazern.lexisoup.feature.search_results.ui.list.model.SelectedHeader
import blazern.lexisoup.feature.search_results.ui.list.model.select
import lexisoup.core.ui.strings.generated.resources.Res
import lexisoup.core.ui.strings.generated.resources.general_lexical_item_detail_type_synonyms
import lexisoup.core.ui.strings.generated.resources.general_lexical_item_detail_type_word_translations

@Composable
internal fun LexicalItemDetailsCardContent(
    detailsGroup: LexicalItemDetailsGroupState.Loaded,
    searchRequest: SearchRequest,
    contentColor: Color,
    callbacks: LexicalItemDetailCallbacks,
) {
    val details = detailsGroup.details
    val source = detailsGroup.source
    val header = SelectedHeader.select(details)
    val detailsFiltered = if (header != null && header.detailConsumed) {
        details.filter { it != header.sourceDetail }
    } else {
        details
    }
    Column(Modifier.fillMaxWidth()) {
        CardHeader(
            header?.text,
            source,
            callbacks,
            translationsSource = extractTranslationsSource(detailsGroup),
        )
        Box {
            DetailsColumn(detailsFiltered, contentColor, callbacks, searchRequest)
            TranslateActions(
                detailsGroup,
                callbacks,
                Modifier.align(Alignment.TopEnd),
            )
        }
    }
}

@Composable
private fun DetailsColumn(
    details: List<LexicalItemDetail>,
    contentColor: Color,
    callbacks: LexicalItemDetailCallbacks,
    searchRequest: SearchRequest,
) {
    val itemPaddings = PaddingValues(vertical = 18.dp)
    Column(
        Modifier.padding(horizontal = 28.dp),
    ) {
        details.compose<Explanation> {
            ExplanationUI(it, contentColor, callbacks, Modifier.padding(itemPaddings))
        }
        // NOTE: this looks terrible, a table is rather needed, like the one that Wiktionary has
//            detailsFiltered.compose<Forms> {
//                Box(Modifier.fillMaxWidth()) {
//                    LexicalItemDetailForms(
//                        it,
//                        contentColor,
//                        callbacks,
//                        Modifier.padding(itemPaddings),
//                    )
//                    Label(
//                        stringResource(Res.string.general_lexical_item_detail_type_forms),
//                        contentColor,
//                    )
//                }
//            }
        details.compose<WordTranslations> {
            Box(Modifier.fillMaxWidth()) {
                SentencesPart(
                    stringResource(Res.string.general_lexical_item_detail_type_word_translations),
                    it.translationsSet.translations,
                    otherLang = searchRequest.langFrom,
                    callbacks,
                    contentColor,
                    Modifier.padding(itemPaddings),
                )
            }
        }
        details.compose<Synonyms> {
            Box(Modifier.fillMaxWidth()) {
                SentencesPart(
                    stringResource(Res.string.general_lexical_item_detail_type_synonyms),
                    it.translationsSet.translations,
                    otherLang = searchRequest.langTo,
                    callbacks,
                    contentColor,
                    Modifier.padding(itemPaddings),
                )
            }
        }
    }
}

@Suppress("LongParameterList")
@Composable
private fun BoxScope.SentencesPart(
    label: String,
    sentences: List<Sentence>,
    otherLang: Lang,
    callbacks: LexicalItemDetailCallbacks,
    textColor: Color,
    modifier: Modifier,
) {
    SentencesList(
        sentences,
        otherLang,
        textColor,
        callbacks,
        modifier,
    )
    Label(label, textColor)
}

@Suppress("ComposableNaming")
@Composable
private inline fun <reified T : LexicalItemDetail> List<LexicalItemDetail>.compose(
    fn: @Composable (T) -> Unit,
) {
    filterIsInstance<T>().forEach { fn(it) }
}

@Composable
private fun BoxScope.Label(
    text: String,
    mainColor: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = mainColor.copy(alpha = 0.2f),
        modifier = modifier
            .align(Alignment.TopStart)
            .padding(top = 4.dp),
    )
}

private fun extractTranslationsSource(group: LexicalItemDetailsGroupState.Loaded): DataSource? {
    for (detail in group.details) {
        when (detail) {
            is Explanation -> {
                extractTranslationsSource(detail)?.let { return it }
            }
            is LexicalItemDetail.Example -> continue // Examples mark their translation source themselves
            is LexicalItemDetail.Forms -> continue // Shouldn't be translated
            is Synonyms -> continue // Shouldn't be translated
            is WordTranslations -> continue // Expected to always match original source
        }
    }
    return null
}

private fun extractTranslationsSource(detail: Explanation): DataSource? {
    detail.translationsSet.translations.forEach { translation ->
        if (translation.source != detail.source) {
            return translation.source
        }
    }
    return null
}
