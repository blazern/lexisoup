package blazern.lexisoup.feature.search_results.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
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
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        detailsGroup.translationStates.forEach { translationState ->
            Box(
                Modifier
                    .minimumInteractiveComponentSize()
                    .padding(top = 4.dp)
            ) {
                when (translationState) {
                    is TranslationState.CanStart -> {
                        IconButton(onClick = {
                            callbacks.onTranslateRequest(
                                detailsGroup,
                                translationState.translationSource
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Translate,
                                contentDescription = stringResource(ResStr.string.general_cd_translate),
                                tint = LocalContentColor.current,
                            )
                        }
                    }
                    is TranslationState.InProgress -> {
                        CircularProgressIndicator(
                            Modifier
                                .size(24.dp)
                                .align(Alignment.Center),
                        )
                    }
                }
            }
        }
    }
}
