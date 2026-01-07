package blazern.lexisoup.utils

import androidx.compose.ui.platform.ClipEntry

actual fun clipEntryOf(string: String) = ClipEntry.withPlainText(string)
