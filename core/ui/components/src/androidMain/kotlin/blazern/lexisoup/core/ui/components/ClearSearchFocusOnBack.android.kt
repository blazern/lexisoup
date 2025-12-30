package blazern.lexisoup.core.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.LocalFocusManager

@OptIn(ExperimentalComposeUiApi::class)
@Composable
actual fun ClearSearchFocusOnBack(
    enabled: Boolean,
    onFocusCleared: ()->Unit,
) {
    val focusManager = LocalFocusManager.current
    BackHandler(enabled = enabled) {
        focusManager.clearFocus(force = true)
        onFocusCleared()
    }
}
