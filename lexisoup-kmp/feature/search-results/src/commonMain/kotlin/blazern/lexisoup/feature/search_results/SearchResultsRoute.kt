package blazern.lexisoup.feature.search_results

import blazern.lexisoup.domain.model.Lang
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import blazern.lexisoup.feature.search_results.ui.SearchResultsScreen
import blazern.lexisoup.feature.search_results.ui.SearchResultsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun SearchResultsRoute(
    query: String,
    langFrom: Lang,
    langTo: Lang,
) {
    val viewModel: SearchResultsViewModel = koinViewModel(
        key = query,
        parameters = { parametersOf(query, langFrom, langTo) },
    )
    val uiState by viewModel.state.collectAsState()
    SearchResultsScreen(
        query,
        uiState,
        onTextCopy = { text, clipboard ->
            viewModel.copyText(text, clipboard)
        },
        onLoadingDetailVisible = {
            viewModel.onLoadingDetailVisible(it)
        },
        onFixErrorRequest = {
            viewModel.onFixErrorRequest(it)
        }
    )
}
