package blazern.lexisoup.feature.search_results

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.settings.SettingsRepository
import blazern.lexisoup.feature.search_results.model.SearchRequest
import blazern.lexisoup.feature.search_results.repository.AudiosPlaybackRepository
import blazern.lexisoup.feature.search_results.repository.BackgroundErrorsRepository
import blazern.lexisoup.feature.search_results.repository.LocalAudiosPlaybackRepository
import blazern.lexisoup.feature.search_results.ui.SearchResultsScreen
import blazern.lexisoup.feature.search_results.ui.SearchResultsViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.component.get
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
    val backgroundErrorsRepo = viewModel.get<BackgroundErrorsRepository>()

    val extraDetailsTypes by viewModel
        .get<SettingsRepository>()
        .getExcludedLexicalItemsDetailsTypes()
        .collectAsState(initial = emptySet())

    CompositionLocalProvider(
        LocalAudiosPlaybackRepository provides viewModel.get<AudiosPlaybackRepository>(),
    ) {
        SearchResultsScreen(
            SearchRequest(query, langFrom, langTo),
            uiState,
            extraDetailsTypes,
            backgroundErrorsRepo.errors,
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
}
