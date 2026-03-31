package blazern.lexisoup.feature.settings

import androidx.compose.runtime.Composable
import blazern.lexisoup.domain.settings.SettingsRepository
import blazern.lexisoup.feature.settings.ui.SettingsScreen
import blazern.lexisoup.feature.settings.ui.SettingsViewModel
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.component.get

@Composable
fun SettingsRoute(
    onPrivacyPolicyClick: () -> Unit,
) {
    val viewModel: SettingsViewModel = koinViewModel()
    val settingsRepo = viewModel.get<SettingsRepository>()
    SettingsScreen(
        onPrivacyPolicyClick = onPrivacyPolicyClick,
        settingsRepo = settingsRepo,
    )
}
