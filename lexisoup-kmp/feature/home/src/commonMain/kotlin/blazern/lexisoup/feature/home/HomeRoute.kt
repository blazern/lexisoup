package blazern.lexisoup.feature.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.feature.home.ui.HomeScreen
import blazern.lexisoup.feature.home.ui.HomeScreenViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

typealias SearchFn = (query: String, langFrom: Lang, langTo: Lang)->Unit

@Composable
fun HomeRoute(
    onSearch: SearchFn,
    onPrivacyPolicyClick: ()->Unit,
) {
    val startQuery = ""
    val viewModel: HomeScreenViewModel = koinViewModel(
        parameters = { parametersOf(startQuery) },
    )
    val uiState by viewModel.state.collectAsState()
    HomeScreen(
        uiState,
        viewModel::onQueryChange,
        viewModel::onLangsChange,
        onSearch,
        viewModel::onLocalhostToggled,
        onPrivacyPolicyClick,
    )
}
