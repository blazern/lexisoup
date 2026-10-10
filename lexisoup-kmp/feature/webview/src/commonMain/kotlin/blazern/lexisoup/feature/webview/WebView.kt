package blazern.lexisoup.feature.webview

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun WebView(
    html: String,
    modifier: Modifier = Modifier,
)
