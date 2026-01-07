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
import blazern.lexisoup.core.ui.strings.stringResource
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.LexicalItemDetail.Forms
import blazern.lexisoup.domain.model.WordForm
import blazern.lexisoup.domain.model.strRsc

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
            stringResource(source.strRsc),
            style = MaterialTheme.typography.labelSmall,
            color = textColor.copy(alpha = 0.2f),
        )
    }
}

internal data class SelectedHeader(
    val text: String,
    val sourceDetail: LexicalItemDetail,
    val detailConsumed: Boolean,
)

internal fun selectHeader(details: List<LexicalItemDetail>): SelectedHeader? {
    val forms = details.filterIsInstance<Forms>().firstOrNull()
    if (forms != null) {
        val value = forms.value
        when (value) {
            is Forms.Value.Text -> return SelectedHeader(
                text = value.text,
                sourceDetail = forms,
                detailConsumed = true,
            )
            is Forms.Value.Detailed -> {
                if (value.forms.isNotEmpty()) {
                    val singular = value.forms
                        .filter { it.tags.any { it is WordForm.Tag.Defined.Singular } }
                        .sortedBy { it.importance }
                        .asReversed()
                        .firstOrNull()
                    val plural = value.forms
                        .filter { it.tags.any { it is WordForm.Tag.Defined.Plural } }
                        .sortedBy { it.importance }
                        .asReversed()
                        .firstOrNull()
                    val text = listOfNotNull(singular?.text, plural?.text)
                        .joinToString(", ")
                    return SelectedHeader(
                        text = text,
                        sourceDetail = forms,
                        detailConsumed = false,
                    )
                }
            }
        }
    }
    return null
}
