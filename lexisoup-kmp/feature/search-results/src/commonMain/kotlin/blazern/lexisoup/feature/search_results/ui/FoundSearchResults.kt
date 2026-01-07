package blazern.lexisoup.feature.search_results.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import blazern.lexisoup.core.ui.theme.LexisoupTheme
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.LexicalItemDetail.Example
import blazern.lexisoup.domain.model.LexicalItemDetail.Explanation
import blazern.lexisoup.domain.model.LexicalItemDetail.Forms
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.TranslationsSet
import blazern.lexisoup.feature.search_results.model.LexicalItemDetailsGroupState
import blazern.lexisoup.feature.search_results.model.SearchResultsState
import blazern.lexisoup.feature.search_results.ui.list.LexicalItemDetailCallbacks
import blazern.lexisoup.feature.search_results.ui.list.ExampleCard
import blazern.lexisoup.feature.search_results.ui.list.ExampleState
import blazern.lexisoup.feature.search_results.ui.list.LexicalItemDetailsCard
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun FoundSearchResults(
    state: SearchResultsState,
    callbacks: LexicalItemDetailCallbacks,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier.padding(horizontal = 16.dp),
    ) {
        val allButExamples = state.groups.filter { it.types != setOf(LexicalItemDetail.Type.EXAMPLE) }
        items(allButExamples.size, { allButExamples[it].id }) {
            LexicalItemDetailsCard(allButExamples[it], callbacks)
        }
        examples(
            state.groups,
            callbacks,
            Modifier.padding(start = 8.dp, end = 8.dp),
        )
        item {
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

private fun LazyListScope.examples(
    groups: List<LexicalItemDetailsGroupState>,
    callbacks: LexicalItemDetailCallbacks,
    modifier: Modifier = Modifier,
) {
    val examples = groups.toExamples()
    items(examples.size) { index ->
        Box(modifier = modifier.animateItem()) {
            ExampleCard(
                examples[index],
                callbacks,
                Modifier.fillMaxWidth(),
            )
        }
    }
}

private fun List<LexicalItemDetailsGroupState>.toExamples(): List<ExampleState> {
    val result = mutableListOf<ExampleState>()
    this.forEach {
        when (it) {
            is LexicalItemDetailsGroupState.Loaded ->
                it.details.filterIsInstance<Example>()
                    .forEach { result.add(ExampleState.Loaded(it)) }
            is LexicalItemDetailsGroupState.Error -> result.add(ExampleState.Error(it))
            is LexicalItemDetailsGroupState.Loading -> result.add(ExampleState.Loading(it))
        }
    }
    return result
}

@Preview(name = "400x500", heightDp = 400, widthDp = 500)
@Composable
private fun Preview() {
    val state = SearchResultsState(
        groups = listOf(
            // Forms group
            LexicalItemDetailsGroupState.Loaded(
                id = "1",
                details = listOf(
                    Forms(
                        Forms.Value.Text("der Hund, -e"),
                        DataSource.CHATGPT,
                    )
                ),
                types = setOf(LexicalItemDetail.Type.FORMS),
                source = DataSource.CHATGPT,
            ),

            // Explanations group
            LexicalItemDetailsGroupState.Loaded(
                id = "2",
                details = listOf(
                    Explanation("Hund is Dog", DataSource.CHATGPT)
                ),
                types = setOf(LexicalItemDetail.Type.EXPLANATION),
                source = DataSource.CHATGPT,
            ),

            // Examples group
            LexicalItemDetailsGroupState.Loaded(
                id = "3",
                details = listOf(
                    Example(
                        TranslationsSet(
                            Sentence("The dog barks", Lang.EN, DataSource.TATOEBA),
                            listOf(Sentence("Der Hund bellt", Lang.DE, DataSource.TATOEBA)),
                            listOf(TranslationsSet.QUALITY_MAX),
                        ),
                        DataSource.CHATGPT,
                    ),
                    Example(
                        TranslationsSet(
                            Sentence("The dog sits", Lang.EN, DataSource.CHATGPT),
                            listOf(
                                Sentence("Der Hund sitzt", Lang.DE, DataSource.CHATGPT),
                                Sentence("Собака сидит", Lang.RU, DataSource.CHATGPT),
                            ),
                            listOf(TranslationsSet.QUALITY_MAX, TranslationsSet.QUALITY_MAX),
                        ),
                        DataSource.CHATGPT,
                    ),
                    Example(
                        TranslationsSet(
                            Sentence("A dog sits on a sofa and looks at me", Lang.EN, DataSource.CHATGPT),
                            listOf(
                                Sentence(
                                    "Ein Hund sitzt auf dem Sofa und guckt mich an",
                                    Lang.DE,
                                    DataSource.CHATGPT,
                                )
                            ),
                            listOf(TranslationsSet.QUALITY_MAX),
                        ),
                        DataSource.CHATGPT,
                    ),
                ),
                types = setOf(LexicalItemDetail.Type.EXAMPLE),
                source = DataSource.CHATGPT,
            ),
        ),
    )

    LexisoupTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            FoundSearchResults(
                state = state,
                callbacks = LexicalItemDetailCallbacks.Stub,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
