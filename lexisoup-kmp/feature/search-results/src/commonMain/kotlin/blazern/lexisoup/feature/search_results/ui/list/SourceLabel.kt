package blazern.lexisoup.feature.search_results.ui.list

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.i18n

@Composable
internal fun SourceLabel(
    text: String,
    mainColor: Color,
    modifier: Modifier = Modifier,
) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        color = mainColor.copy(alpha = 0.2f),
        modifier = modifier,
    )
}
