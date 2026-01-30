package blazern.lexisoup.feature.search_results.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.i18n

@Suppress("MagicNumber")
@Composable
internal fun CardHeader(
    headerText: String?,
    source: DataSource,
    callbacks: LexicalItemDetailCallbacks,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
) {
    Row(modifier) {
        if (headerText != null) {
            Text(
                headerText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier.weight(0.8f).clickable {
                    callbacks.onTextCopy(headerText)
                }
            )
        } else {
            Spacer(Modifier.weight(0.8f))
        }
        Text(
            source.i18n(),
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.2f),
        )
    }
}
