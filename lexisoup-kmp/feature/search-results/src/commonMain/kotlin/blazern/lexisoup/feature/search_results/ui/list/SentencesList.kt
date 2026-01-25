package blazern.lexisoup.feature.search_results.ui.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import blazern.lexisoup.core.ui.components.Expandable
import blazern.lexisoup.core.ui.strings.stringResource
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.feature.search_results.model.SearchRequest
import blazern.lexisoup.feature.search_results.ui.ContextAction
import blazern.lexisoup.feature.search_results.ui.ContextMenuTrigger
import lexisoup.core.ui.strings.generated.resources.Res
import lexisoup.core.ui.strings.generated.resources.general_action_collapse
import lexisoup.core.ui.strings.generated.resources.general_action_copy
import lexisoup.core.ui.strings.generated.resources.general_action_expand
import lexisoup.core.ui.strings.generated.resources.general_action_search
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
internal fun SentencesList(
    sentences: List<Sentence>,
    otherLang: Lang,
    contentColor: Color,
    callbacks: LexicalItemDetailCallbacks,
    modifier: Modifier = Modifier,
) {
    Box {
        Expandable(
            collapsedMaxHeight = 145.dp,
            control = { expanded, canExpand, onToggle ->
                if (canExpand) {
                    IconButton(
                        onClick = onToggle,
                        modifier = Modifier.align(Alignment.BottomEnd),
                        colors = IconButtonDefaults.iconButtonColors(
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = if (expanded) Icons.Filled.Close else Icons.Filled.Add,
                            contentDescription = if (expanded) {
                                stringResource(Res.string.general_action_collapse)
                            } else {
                                stringResource(Res.string.general_action_expand)
                            },
                            tint = contentColor,
                        )
                    }
                }
            },
        ) {
            FlowRow(modifier = modifier) {
                sentences.forEachIndexed { inx, sentence ->
                    ContextMenuTrigger(
                        actions = listOf(
                            ContextAction(stringResource(Res.string.general_action_copy)) {
                                callbacks.onTextCopy(sentence.text)
                            },
                            ContextAction(stringResource(Res.string.general_action_search)) {
                                callbacks.onNewSearch(SearchRequest(
                                    query = sentence.text,
                                    langFrom = sentence.lang,
                                    langTo = otherLang,
                                ))
                            },
                        )
                    ) {
                        Text(sentence.text, color = contentColor)
                    }
                    if (inx != sentences.size - 1) {
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(
                                "·",
                                color = contentColor
                            )
                        }
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    val sentences = listOf(
        Sentence("dog", Lang.DE, DataSource.KAIKKI),
        Sentence("hound", Lang.DE, DataSource.KAIKKI),
        Sentence("mutt", Lang.DE, DataSource.KAIKKI),
        Sentence("human's best friend", Lang.DE, DataSource.KAIKKI),
    )
    MaterialTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            SentencesList(
                sentences,
                otherLang = Lang.EN,
                contentColor = MaterialTheme.colorScheme.onBackground,
                callbacks = LexicalItemDetailCallbacks.Stub,
                modifier = Modifier.padding(innerPadding),
            )
        }
    }
}
