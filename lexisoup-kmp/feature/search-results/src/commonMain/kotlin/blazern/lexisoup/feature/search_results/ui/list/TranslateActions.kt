package blazern.lexisoup.feature.search_results.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import blazern.lexisoup.core.ui.strings.stringResource
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.feature.search_results.model.LexicalItemDetailsGroupState
import blazern.lexisoup.feature.search_results.model.TranslationState
import lexisoup.core.ui.strings.generated.resources.general_action_cancel
import lexisoup.core.ui.strings.generated.resources.general_action_download
import lexisoup.core.ui.strings.generated.resources.general_cd_translate
import lexisoup.core.ui.strings.generated.resources.search_results_download_langs_dialog_text
import lexisoup.core.ui.strings.generated.resources.search_results_download_langs_dialog_title
import lexisoup.core.ui.strings.generated.resources.Res as ResStr

@Composable
internal fun TranslateActions(
    detailsGroup: LexicalItemDetailsGroupState.Loaded,
    callbacks: LexicalItemDetailCallbacks,
    modifier: Modifier = Modifier,
) {
    val translateRequest = remember {
        mutableStateOf<Pair<LexicalItemDetailsGroupState.Loaded, DataSource>?>(null)
    }

    Column(modifier) {
        detailsGroup.translationStates.forEach { translationState ->
            Box(
                Modifier
                    .minimumInteractiveComponentSize()
                    .padding(top = 4.dp)
            ) {
                when (translationState) {
                    is TranslationState.MustDownloadLangs,
                    is TranslationState.CanStart -> {
                        IconButton(onClick = {
                            if (translationState is TranslationState.MustDownloadLangs) {
                                translateRequest.value = Pair(
                                    detailsGroup,
                                    translationState.translationSource,
                                )
                            } else {
                                callbacks.onTranslateRequest(
                                    detailsGroup,
                                    translationState.translationSource
                                )
                            }
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

    DownloadLangsDialog(translateRequest, callbacks)
}

@Composable
private fun DownloadLangsDialog(
    translateRequest: MutableState<Pair<LexicalItemDetailsGroupState.Loaded, DataSource>?>,
    callbacks: LexicalItemDetailCallbacks
) {
    if (translateRequest.value != null) {
        AlertDialog(
            onDismissRequest = {
                translateRequest.value = null
            },
            title = {
                Text(stringResource(ResStr.string.search_results_download_langs_dialog_title))
            },
            text = {
                Text(stringResource(ResStr.string.search_results_download_langs_dialog_text))
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        translateRequest.value?.let {
                            callbacks.onTranslateRequest(it.first, it.second)
                        }
                        translateRequest.value = null
                    }
                ) {
                    Text(stringResource(ResStr.string.general_action_download))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        translateRequest.value = null
                    }
                ) {
                    Text(stringResource(ResStr.string.general_action_cancel))
                }
            },
        )
    }
}
