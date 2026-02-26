package blazern.lexisoup.feature.search_results.ui.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TextAccent
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.feature.search_results.model.LexicalItemDetailsGroupState
import blazern.lexisoup.feature.search_results.model.TranslationState
import blazern.lexisoup.feature.search_results.ui.list.model.ExampleState
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
internal fun ExampleCard(
    state: ExampleState,
    callbacks: LexicalItemDetailCallbacks,
    modifier: Modifier = Modifier,
) {
    val cardColors = if (state !is ExampleState.Error) {
        CardDefaults.cardColors()
    } else {
        CardDefaults.cardColors().copy(
            containerColor = MaterialTheme.colorScheme.error,
            contentColor = MaterialTheme.colorScheme.onError,
        )
    }
    Card(
        colors = cardColors,
        modifier = modifier,
    ) {
        when (state) {
            is ExampleState.Loaded -> {
                val bg1 = MaterialTheme.colorScheme.secondaryContainer
                val bg2 = MaterialTheme.colorScheme.surfaceContainer
                ExampleContent(
                    loadedExample = state,
                    contentColor = cardColors.contentColor,
                    backgroundColor1 = bg1,
                    backgroundColor2 = bg2,
                    callbacks = callbacks,
                )
            }
            is ExampleState.Error -> ErrorContent(
                state.error,
                cardColors.contentColor,
                callbacks,
            )
            is ExampleState.Loading -> {
                callbacks.onLoadingDetailVisible(state.loading)
                LoadingContent(state.loading.source, callbacks)
            }
        }
    }
}

@Composable
internal fun ExampleContent(
    loadedExample: ExampleState.Loaded,
    callbacks: LexicalItemDetailCallbacks,
    contentColor: Color,
    backgroundColor1: Color,
    backgroundColor2: Color,
    modifier: Modifier = Modifier,
) {
    val example = loadedExample.example
    val sentences = mutableListOf<Sentence>()
    sentences += example.translationsSet.original
    example.translationsSet.translations.forEach {
        sentences += it
    }
    Column(modifier) {
        sentences.forEachIndexed { index, sentence ->
            val (color, textColor) = if (index == 0) {
                backgroundColor1 to contentColor
            } else {
                backgroundColor2 to contentColor
            }
            val actionsUI = if (sentences.size == 1) {
                @Composable {
                    TranslateActions(loadedExample.parentGroup, callbacks)
                }
            } else {
                {}
            }
            SentenceLine(
                sentence,
                color,
                textColor,
                callbacks,
                actionsUI,
            )
        }
    }
}

@Composable
private fun SentenceLine(
    sentence: Sentence,
    color: Color,
    textColor: Color,
    callbacks: LexicalItemDetailCallbacks,
    actionsUI: @Composable () -> Unit,
) {
    val textAccented = remember(sentence.text, sentence.textAccents) {
        buildAnnotatedString {
            append(sentence.text)
            sentence.textAccents.forEach {
                addStyle(SpanStyle(
                    fontWeight = FontWeight.Bold),
                    start = it.start,
                    end = it.end,
                )
            }
        }
    }

    Box(contentAlignment = Alignment.Center,
        modifier = Modifier
            .heightIn(min = 40.dp)
            .fillMaxWidth()
            .background(color)
            .clickable { callbacks.onTextCopy(sentence.text) },
    ) {
        Text(
            textAccented,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(
                horizontal = 20.dp,
                vertical = 14.dp,
            )
        )
        SourceLabel(
            sentence.source,
            textColor,
            Modifier
                .align(Alignment.TopEnd)
                .padding(end = 8.dp, top = 4.dp),
        )
        Box(Modifier
            .align(Alignment.BottomEnd)
            .padding(end = 8.dp, bottom = 4.dp),) {
            actionsUI()
        }
    }
}


@Suppress("MagicNumber")
@Preview(heightDp = 800, widthDp = 500)
@Composable
private fun PreviewAll() {
    val examples1 = listOf(
        LexicalItemDetail.Example(
            TranslationsSet(
                Sentence("Hund", Lang.DE, DataSource.Kaikki),
                listOf(Sentence("dog", Lang.EN, DataSource.Kaikki)),
                listOf(TranslationsSet.QUALITY_MAX),
            ),
            DataSource.Kaikki,
        ),
        LexicalItemDetail.Example(
            TranslationsSet(
                Sentence(
                    "Der Hund sitzt",
                    Lang.DE,
                    DataSource.Kaikki,
                    setOf(TextAccent(4, 8)),
                ),
                emptyList(),
                listOf(TranslationsSet.QUALITY_MAX),
            ),
            DataSource.Kaikki,
        ),
    )
    val examplesGroup1 = LexicalItemDetailsGroupState.Loaded(
        id = "1",
        details = examples1,
        types = setOf(LexicalItemDetail.Type.EXAMPLE),
        source = DataSource.Kaikki,
        translationStates = listOf(
            TranslationState.CanStart(DataSource.Backend(DataSource.DeepL)),
        )
    )

    val examples2 = listOf(
        LexicalItemDetail.Example(
            TranslationsSet(
                Sentence(
                    "Mein Lieblingshund",
                    Lang.DE,
                    DataSource.ChatGPT,
                    setOf(TextAccent(14, 18))
                ),
                listOf(Sentence("my favorite dog", Lang.DE, DataSource.ChatGPT)),
                listOf(TranslationsSet.QUALITY_MAX),
            ),
            DataSource.ChatGPT,
        ),
    )
    val examplesGroup2 = LexicalItemDetailsGroupState.Loaded(
        id = "1",
        details = examples2,
        types = setOf(LexicalItemDetail.Type.EXAMPLE),
        source = DataSource.Kaikki,
        translationStates = listOf(
            TranslationState.InProgress(DataSource.Backend(DataSource.DeepL)),
        )
    )

    MaterialTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).padding(top = 32.dp, start = 16.dp, end = 16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    examples1.forEach {
                        ExampleCard(ExampleState.Loaded(it, examplesGroup1), LexicalItemDetailCallbacks.Stub)
                    }
                    ExampleCard(
                        ExampleState.Loading(
                            LexicalItemDetailsGroupState.Loading(
                                id = "1",
                                setOf(LexicalItemDetail.Type.EXAMPLE),
                                DataSource.Tatoeba,
                            ),
                        ),
                        LexicalItemDetailCallbacks.Stub,
                    )
                    examples2.forEach {
                        ExampleCard(ExampleState.Loaded(it, examplesGroup2), LexicalItemDetailCallbacks.Stub)
                    }
                    ExampleCard(
                        ExampleState.Error(
                            LexicalItemDetailsGroupState.Error(
                                id = "2",
                                Err.Other(null),
                                setOf(LexicalItemDetail.Type.EXAMPLE),
                                DataSource.Tatoeba,
                            ),
                        ),
                        LexicalItemDetailCallbacks.Stub,
                    )
                }
            }
        }
    }
}
