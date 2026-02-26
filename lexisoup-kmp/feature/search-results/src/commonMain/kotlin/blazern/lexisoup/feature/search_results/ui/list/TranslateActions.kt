package blazern.lexisoup.feature.search_results.ui.list

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import blazern.lexisoup.core.ui.strings.stringResource
import blazern.lexisoup.feature.search_results.model.LexicalItemDetailsGroupState
import blazern.lexisoup.feature.search_results.model.TranslationState
import lexisoup.core.ui.strings.generated.resources.general_cd_translate
import lexisoup.core.ui.strings.generated.resources.Res as ResStr

@Composable
internal fun TranslateActions(
    detailsGroup: LexicalItemDetailsGroupState.Loaded,
    callbacks: LexicalItemDetailCallbacks,
) {
    Row {
        detailsGroup.translationStates.forEach { translationState ->
            when (translationState) {
                is TranslationState.CanStart -> {
                    IconButton(onClick = {
                        callbacks.onTranslateRequest(detailsGroup, translationState.translationSource)
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Translate,
                            contentDescription = stringResource(ResStr.string.general_cd_translate),
                            tint = LocalContentColor.current,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
                is TranslationState.InProgress -> {
                    CircularProgressIndicator(Modifier.size(16.dp))
                }
            }
        }
    }
}
