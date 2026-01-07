package blazern.lexisoup.utils

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry

actual fun clipEntryOf(string: String) = ClipEntry(
    ClipData.newPlainText(
        string, string
    )
)
