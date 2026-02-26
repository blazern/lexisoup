package blazern.lexisoup.feature.search_results.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import blazern.lexisoup.core.ui.strings.stringResource
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.i18n
import lexisoup.core.ui.strings.generated.resources.Res
import lexisoup.core.ui.strings.generated.resources.general_action_collapse
import lexisoup.core.ui.strings.generated.resources.general_action_expand

@Suppress("MagicNumber")
@Composable
internal fun CardHeader(
    headerText: String?,
    source: DataSource,
    callbacks: LexicalItemDetailCallbacks,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    actionsUI: @Composable () -> Unit = {},
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
        actionsUI()
        SourceLabel(source, textColor)
    }
}
