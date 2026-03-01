package blazern.lexisoup.feature.search_results.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import blazern.lexisoup.core.ui.strings.stringResource
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.i18n
import lexisoup.core.ui.strings.generated.resources.Res
import lexisoup.core.ui.strings.generated.resources.general_translated_by_postfix

@Suppress("MagicNumber")
@Composable
internal fun CardHeader(
    headerText: String?,
    source: DataSource,
    callbacks: LexicalItemDetailCallbacks,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onBackground,
    translationsSource: DataSource? = null,
) {
    Box(contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxWidth()
    ) {
        if (headerText != null) {
            Text(
                headerText,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = textColor,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(start = 20.dp, end = 20.dp, top = 14.dp)
                    .clickable {
                        callbacks.onTextCopy(headerText)
                    }
            )
        }
        val translationsSourceText = translationsSource
            ?.let { stringResource(Res.string.general_translated_by_postfix) + it.i18n() }
            ?: ""
        SourceLabel(
            source.i18n() + translationsSourceText,
            textColor,
            Modifier
                .align(Alignment.TopEnd)
                .padding(end = 8.dp, top = 4.dp),
        )
    }
}
