package blazern.lexisoup.feature.search_results.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import blazern.lexisoup.domain.model.LexicalItemDetail.Explanation

@Composable
internal fun ExplanationUI(
    explanation: Explanation,
    contentColor: Color,
    callbacks: LexicalItemDetailCallbacks,
    modifier: Modifier,
) {
    Column(modifier) {
        Text(
            explanation.translationsSet.original.text,
            color = contentColor,
            modifier = Modifier.clickable {
                callbacks.onTextCopy(explanation.translationsSet.original.text)
            }
        )
        explanation.translationsSet.translations.forEach { translation ->
            Text(
                translation.text,
                color = contentColor,
                fontStyle = FontStyle.Italic,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .clickable {
                        callbacks.onTextCopy(translation.text)
                    }
            )
        }
    }
}
