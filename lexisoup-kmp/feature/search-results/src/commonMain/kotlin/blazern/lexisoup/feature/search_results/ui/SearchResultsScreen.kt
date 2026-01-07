package blazern.lexisoup.feature.search_results.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.Clipboard
import androidx.compose.ui.platform.LocalClipboard
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import blazern.lexisoup.domain.error.Err
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
import blazern.lexisoup.core.ui.strings.stringResource
import blazern.lexisoup.core.ui.theme.LexisoupTheme
import kotlinx.coroutines.launch
import lexisoup.core.ui.strings.generated.resources.Res
import lexisoup.core.ui.strings.generated.resources.general_copied_to_clipboard
import lexisoup.core.ui.strings.generated.resources.search_results_title
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun SearchResultsScreen(
    query: String,
    state: SearchResultsState,
    onTextCopy: (String, Clipboard)->Unit,
    onLoadingDetailVisible: (LexicalItemDetailsGroupState.Loading) -> Unit,
    onFixErrorRequest: (LexicalItemDetailsGroupState.Error) -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val copiedMsg = stringResource(Res.string.general_copied_to_clipboard)
    val clipboard = LocalClipboard.current
    val onTextCopyWrapper = { text: String ->
        onTextCopy.invoke(text, clipboard)
        scope.launch {
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(copiedMsg)
        }
        Unit
    }

    Scaffold(
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState)
        },
        modifier = Modifier.fillMaxSize(),
        contentWindowInsets = WindowInsets.systemBars.only(WindowInsetsSides.Horizontal)
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            Box(Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(top = 24.dp, bottom = 8.dp, start = 16.dp, end = 16.dp)
            ) {
                FlowRow {
                    Text(
                        stringResource(Res.string.search_results_title) + " ",
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        query,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
            val callbacks = object : LexicalItemDetailCallbacks {
                override fun onTextCopy(text: String) = onTextCopyWrapper(text)
                override fun onLoadingDetailVisible(loading: LexicalItemDetailsGroupState.Loading) =
                    onLoadingDetailVisible(loading)
                override fun onFixErrorRequest(error: LexicalItemDetailsGroupState.Error) =
                    onFixErrorRequest(error)
            }
            FoundSearchResults(
                state = state,
                callbacks = callbacks,
            )
        }
    }
}

@Preview(name = "400x500", heightDp = 400, widthDp = 500)
@Composable
private fun PreviewAllGood() {
    val state = SearchResultsState(
        groups = listOf(
            // Forms
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

            // Explanations
            LexicalItemDetailsGroupState.Loaded(
                id = "2",
                details = listOf(
                    Explanation("Hund is Dog", DataSource.CHATGPT)
                ),
                types = setOf(LexicalItemDetail.Type.EXPLANATION),
                source = DataSource.CHATGPT,
            ),

            // Examples
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
                            listOf(TranslationsSet.QUALITY_MAX, TranslationsSet.QUALITY_MAX),
                        ),
                        DataSource.CHATGPT,
                    ),
                ),
                types = setOf(LexicalItemDetail.Type.EXAMPLE),
                source = DataSource.CHATGPT,
            ),
        )
    )

    LexisoupTheme {
        SearchResultsScreen(
            query = "Hund",
            state = state,
            onTextCopy = { _, _ -> },
            onLoadingDetailVisible = {},
            onFixErrorRequest = {},
        )
    }
}

@Preview(name = "400x500", heightDp = 400, widthDp = 500)
@Composable
private fun PreviewErrors() {
    val state = SearchResultsState(
        groups = listOf(
            // Forms error
            LexicalItemDetailsGroupState.Error(
                id = "1",
                err = Err.Other(null),
                types = setOf(LexicalItemDetail.Type.FORMS),
                source = DataSource.CHATGPT,
            ),
            // Explanations error
            LexicalItemDetailsGroupState.Error(
                id = "2",
                err = Err.Other(null),
                types = setOf(LexicalItemDetail.Type.EXPLANATION),
                source = DataSource.CHATGPT,
            ),
            // Examples error from TATOEBA
            LexicalItemDetailsGroupState.Error(
                id = "3",
                err = Err.Other(null),
                types = setOf(LexicalItemDetail.Type.EXAMPLE),
                source = DataSource.TATOEBA,
            ),
            // Examples error from ChatGPT
            LexicalItemDetailsGroupState.Error(
                id = "3",
                err = Err.Other(null),
                types = setOf(LexicalItemDetail.Type.EXAMPLE),
                source = DataSource.CHATGPT,
            ),
        )
    )

    LexisoupTheme {
        SearchResultsScreen(
            query = "Herangehensweise",
            state = state,
            onTextCopy = { _, _ -> },
            onLoadingDetailVisible = {},
            onFixErrorRequest = {},
        )
    }
}
