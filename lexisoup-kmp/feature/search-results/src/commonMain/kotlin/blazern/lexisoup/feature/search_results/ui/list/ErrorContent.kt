package blazern.lexisoup.feature.search_results.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import blazern.lexisoup.feature.search_results.model.LexicalItemDetailsGroupState

@Composable
internal fun ErrorContent(
    error: LexicalItemDetailsGroupState.Error,
    textColor: Color,
    callbacks: LexicalItemDetailCallbacks,
    modifier: Modifier = Modifier,
) {
    val errorText = error.err.e.toString()
    Column(modifier = modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp, vertical = 16.dp)
        .clickable { callbacks.onFixErrorRequest(error) }
    ) {
        CardHeader(
            headerText = null,
            error.source,
            callbacks,
            textColor = textColor,
        )
        Text(errorText, color = textColor)
    }
}
