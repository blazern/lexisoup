package blazern.lexisoup.feature.search_results

import blazern.lexisoup.domain.model.Lang
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import blazern.lexisoup.feature.search_results.model.SearchRequest
import blazern.lexisoup.feature.search_results.ui.SearchResultsScreen
import blazern.lexisoup.feature.search_results.ui.SearchResultsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

typealias SearchFn = (query: String, langFrom: Lang, langTo: Lang)->Unit

@Composable
fun SearchResultsRoute(
    query: String,
    langFrom: Lang,
    langTo: Lang,
    onNewSearch: SearchFn,
) {
    val viewModel: SearchResultsViewModel = koinViewModel(
        key = query,
        parameters = { parametersOf(query, langFrom, langTo) },
    )
    val uiState by viewModel.state.collectAsState()
    SearchResultsScreen(
        SearchRequest(query, langFrom, langTo),
        uiState,
        viewModel.backgroundErrors,
        onTextCopy = { text, clipboard ->
            viewModel.copyText(text, clipboard)
        },
        onLoadingDetailVisible = {
            viewModel.onLoadingDetailVisible(it)
        },
        onFixErrorRequest = {
            viewModel.onFixErrorRequest(it)
        },
        onNewSearch = {
            onNewSearch(it.query, it.langFrom, it.langTo)
        },
        onTranslateRequest = { detailsGroup, translator ->
            viewModel.onTranslateRequest(detailsGroup, translator)
        },
    )
}
