package blazern.lexisoup.feature.search_results.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.Sentence

@Composable
internal fun LexicalItemDetailForms(
    forms: LexicalItemDetail.Forms,
    textColor: Color,
    callbacks: LexicalItemDetailCallbacks,
    modifier: Modifier,
) {
    when (val value = forms.value) {
        is LexicalItemDetail.Forms.Value.Text -> {
            Box(modifier.clickable { callbacks.onTextCopy(value.text) }) {
                Text(value.text, color = textColor)
            }
        }
        is LexicalItemDetail.Forms.Value.Detailed -> {
            SentencesList(
                value.forms.map { Sentence(it.text, it.lang, forms.source) },
                textColor,
                callbacks,
                modifier,
            )
        }
    }
}
