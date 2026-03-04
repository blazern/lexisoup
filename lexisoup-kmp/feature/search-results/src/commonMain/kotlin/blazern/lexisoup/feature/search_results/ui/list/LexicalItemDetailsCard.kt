package blazern.lexisoup.feature.search_results.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import blazern.lexisoup.domain.error.Err
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.LexicalItemDetail.Explanation
import blazern.lexisoup.domain.model.LexicalItemDetail.Forms
import blazern.lexisoup.domain.model.LexicalItemDetail.Synonyms
import blazern.lexisoup.domain.model.LexicalItemDetail.WordTranslations
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.domain.model.TranslationsSet.Companion.QUALITY_BASIC
import blazern.lexisoup.domain.model.WordForm
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Genitive
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Nominative
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Plural
import blazern.lexisoup.domain.model.WordForm.Tag.Defined.Singular
import blazern.lexisoup.feature.search_results.model.LexicalItemDetailsGroupState
import blazern.lexisoup.feature.search_results.model.SearchRequest
import blazern.lexisoup.feature.search_results.model.TranslationState
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun LexicalItemDetailsCard(
    state: LexicalItemDetailsGroupState,
    searchRequest: SearchRequest,
    callbacks: LexicalItemDetailCallbacks,
    extraDetailsTypes: Set<LexicalItemDetail.Type> = setOf(LexicalItemDetail.Type.ETYMOLOGY),
    initiallyShowExtraDetails: Boolean = false,
) {
    val defaultColors = CardDefaults.cardColors()
    val isError = state is LexicalItemDetailsGroupState.Error
    val containerColor = if (isError) MaterialTheme.colorScheme.error else defaultColors.containerColor
    val contentColor = if (isError) MaterialTheme.colorScheme.onError else defaultColors.contentColor
    val containerColorAnimated by animateColorAsState(containerColor)
    val contentColorAnimated by animateColorAsState(contentColor)

    Card(
        colors = CardDefaults.cardColors(
            containerColor = containerColorAnimated,
            contentColor = contentColorAnimated,
        ),
        modifier = Modifier.animateContentSize(animationSpec = spring()),
    ) {
        if (state is LexicalItemDetailsGroupState.Loading) {
            LaunchedEffect(state.id) {
                callbacks.onLoadingDetailVisible(state)
            }
        }

        Crossfade(targetState = state, label = "lexicalItemState") { target ->
            when (target) {
                is LexicalItemDetailsGroupState.Loaded -> {
                    LexicalItemDetailsCardContent(
                        target,
                        searchRequest,
                        contentColorAnimated,
                        callbacks,
                        extraDetailsTypes,
                        initiallyShowExtraDetails,
                    )
                }
                is LexicalItemDetailsGroupState.Loading -> {
                    LoadingContent(target.source, callbacks)
                }
                is LexicalItemDetailsGroupState.Error -> {
                    ErrorContent(
                        target,
                        contentColorAnimated,
                        callbacks,
                    )
                }
            }
        }
    }
}

@Preview(heightDp = 1000, widthDp = 500)
@Composable
private fun PreviewCards() {
    val searchRequest = SearchRequest("Hund", Lang.DE, Lang.EN)
    val translations = TranslationsSet(
        Sentence("Hund", Lang.EN, DataSource.Kaikki),
        listOf(
            Sentence("dog", Lang.DE, DataSource.Kaikki),
            Sentence("hound", Lang.DE, DataSource.Kaikki),
            Sentence("mutt", Lang.DE, DataSource.Kaikki),
            Sentence("human's best friend", Lang.DE, DataSource.Kaikki),
        ),
        listOf(TranslationsSet.QUALITY_MAX, TranslationsSet.QUALITY_MAX, TranslationsSet.QUALITY_MAX),
    )
    val synonyms = TranslationsSet(
        Sentence("Hund", Lang.EN, DataSource.Kaikki),
        listOf(
            Sentence("Hündin", Lang.DE, DataSource.Kaikki),
            Sentence("Wauwau", Lang.DE, DataSource.Kaikki),
        ),
        listOf(TranslationsSet.QUALITY_MAX, TranslationsSet.QUALITY_MAX, TranslationsSet.QUALITY_MAX),
    )
    val fancyForms = Forms(
        value = Forms.Value.Detailed(listOf(
            WordForm("der Hund", Lang.DE, listOf(Singular(""), Nominative(""))),
            WordForm("die Hunde", Lang.DE, listOf(Plural(""), Nominative(""))),
            WordForm("des Hundes", Lang.DE, listOf(Singular(""), Genitive(""))),
            WordForm("des Hunds", Lang.DE, listOf(Singular(""), Genitive(""))),
        )),
        lang = Lang.DE,
        source = DataSource.Kaikki,
    )

    val explanation = """
        Hund means dog, it's a common domestic animal loved by many as a pet.
        Dogs are known for being loyal and are often kept as companionship or work"""
        .trimIndent()
        .replace(Regex("\n"), " ")
    MaterialTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).padding(top = 32.dp, start = 16.dp, end = 16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LexicalItemDetailsCard(
                        LexicalItemDetailsGroupState.Loaded(
                            id = "1",
                            listOf(
                                Explanation(
                                    Sentence(explanation, Lang.EN, DataSource.Kaikki),
                                    DataSource.Kaikki,
                                ),
                                Forms(Forms.Value.Text("der Hund, -e"), Lang.DE, DataSource.Kaikki),
                                WordTranslations(translations, DataSource.Kaikki),
                                Synonyms(synonyms, DataSource.Kaikki),
                                LexicalItemDetail.Etymology(
                                    TranslationsSet(
                                        Sentence("Hund stammt von 'hunt' ab", Lang.DE, DataSource.Kaikki),
                                    ),
                                    DataSource.Kaikki,
                                ),
                            ),
                            LexicalItemDetail.Type.entries.toSet(),
                            DataSource.Kaikki,
                            listOf(
                                TranslationState.InProgress(DataSource.Backend(DataSource.DeepL))
                            ),
                        ),
                        searchRequest,
                        LexicalItemDetailCallbacks.Stub,
                        initiallyShowExtraDetails = true,
                    )
                    LexicalItemDetailsCard(
                        LexicalItemDetailsGroupState.Loaded(
                            id = "2",
                            listOf(
                                WordTranslations(translations, DataSource.Kaikki),
                                Synonyms(synonyms, DataSource.Kaikki),
                            ),
                            LexicalItemDetail.Type.entries.toSet(),
                            DataSource.Kaikki,
                            translationStates = emptyList(),
                        ),
                        searchRequest,
                        LexicalItemDetailCallbacks.Stub,
                    )
                    LexicalItemDetailsCard(
                        LexicalItemDetailsGroupState.Loaded(
                            id = "3",
                            listOf(
                                fancyForms,
                                Explanation(
                                    TranslationsSet(
                                        Sentence("Hund is ein Haustier", Lang.DE, DataSource.Kaikki),
                                        listOf(Sentence("Dog is a pet", Lang.EN, DataSource.DeepL)),
                                        listOf(QUALITY_BASIC),
                                    ),
                                    DataSource.Kaikki,
                                ),
                                Explanation(
                                    TranslationsSet(
                                        Sentence("Haustier, dessen Forfahre der Wolf ist", Lang.DE, DataSource.Kaikki),
                                        listOf(Sentence("Pet whose ancestor is the wolf", Lang.EN, DataSource.DeepL)),
                                        listOf(QUALITY_BASIC),
                                    ),
                                    DataSource.Kaikki,
                                ),
                                WordTranslations(translations, DataSource.Kaikki),
                                Synonyms(synonyms, DataSource.Kaikki),
                                LexicalItemDetail.Etymology(
                                    TranslationsSet(
                                        Sentence("Hund stammt von 'hunt' ab", Lang.DE, DataSource.Kaikki),
                                    ),
                                    DataSource.Kaikki,
                                ),
                            ),
                            LexicalItemDetail.Type.entries.toSet(),
                            DataSource.Kaikki,
                            listOf(
                                TranslationState.CanStart(DataSource.Backend(DataSource.DeepL))
                            ),
                        ),
                        searchRequest,
                        LexicalItemDetailCallbacks.Stub,
                    )
                }
            }
        }
    }
}

@Preview(heightDp = 600, widthDp = 500)
@Composable
private fun PreviewAll() {
    val searchRequest = SearchRequest("Hund", Lang.DE, Lang.EN)
    val translations = TranslationsSet(
        Sentence("Hund", Lang.EN, DataSource.Kaikki),
        listOf(
            Sentence("dog", Lang.DE, DataSource.Kaikki),
            Sentence("hound", Lang.DE, DataSource.Kaikki),
            Sentence("mutt", Lang.DE, DataSource.Kaikki),
            Sentence("human's best friend", Lang.DE, DataSource.Kaikki),
        ),
        listOf(TranslationsSet.QUALITY_MAX, TranslationsSet.QUALITY_MAX, TranslationsSet.QUALITY_MAX),
    )
    val synonyms = TranslationsSet(
        Sentence("Hund", Lang.EN, DataSource.Kaikki),
        listOf(
            Sentence("Hündin", Lang.DE, DataSource.Kaikki),
            Sentence("Wauwau", Lang.DE, DataSource.Kaikki),
        ),
        listOf(TranslationsSet.QUALITY_MAX, TranslationsSet.QUALITY_MAX, TranslationsSet.QUALITY_MAX),
    )

    MaterialTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            Box(modifier = Modifier.padding(innerPadding).padding(top = 32.dp, start = 16.dp, end = 16.dp)) {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    LexicalItemDetailsCard(
                        LexicalItemDetailsGroupState.Loaded(
                            id = "1",
                            listOf(
                                Forms(Forms.Value.Text("der Hund, -e"), Lang.DE, DataSource.Kaikki),
                                WordTranslations(translations, DataSource.Kaikki),
                                Synonyms(synonyms, DataSource.Kaikki),
                            ),
                            LexicalItemDetail.Type.entries.toSet(),
                            DataSource.Kaikki,
                            translationStates = emptyList(),
                        ),
                        searchRequest,
                        LexicalItemDetailCallbacks.Stub,
                    )
                    LexicalItemDetailsCard(
                        LexicalItemDetailsGroupState.Loading(
                            id = "2",
                            LexicalItemDetail.Type.entries.toSet(),
                            DataSource.Kaikki,
                        ),
                        searchRequest,
                        LexicalItemDetailCallbacks.Stub,
                    )
                    LexicalItemDetailsCard(
                        LexicalItemDetailsGroupState.Error(
                            id = "3",
                            Err.Other(null),
                            LexicalItemDetail.Type.entries.toSet(),
                            DataSource.Kaikki,
                        ),
                        searchRequest,
                        LexicalItemDetailCallbacks.Stub,
                    )
                }
            }
        }
    }
}