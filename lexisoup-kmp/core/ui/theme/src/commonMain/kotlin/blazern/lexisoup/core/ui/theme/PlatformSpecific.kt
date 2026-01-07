package blazern.lexisoup.core.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
expect fun getPlatformColorScheme(darkTheme: Boolean): ColorScheme?
