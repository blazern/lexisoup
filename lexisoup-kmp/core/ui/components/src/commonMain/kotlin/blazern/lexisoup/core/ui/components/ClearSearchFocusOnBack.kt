package blazern.lexisoup.core.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun ClearSearchFocusOnBack(
    enabled: Boolean,
    onFocusCleared: ()->Unit,
)
