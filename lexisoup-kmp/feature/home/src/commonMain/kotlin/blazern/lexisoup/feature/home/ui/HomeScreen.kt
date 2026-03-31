package blazern.lexisoup.feature.home.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.intl.LocaleList
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import blazern.lexisoup.core.ui.components.ClearSearchFocusOnBack
import blazern.lexisoup.core.ui.components.SearchBar
import blazern.lexisoup.core.ui.components.icons.IconSwitch
import blazern.lexisoup.core.ui.strings.stringResource
import blazern.lexisoup.core.ui.theme.LexisoupTheme
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.Sentence
import blazern.lexisoup.domain.model.Suggestion
import blazern.lexisoup.feature.home.SearchFn
import blazern.lexisoup.feature.home.model.HomeScreenState
import lexisoup.core.ui.strings.generated.resources.home_cd_clear_search_query
import lexisoup.core.ui.strings.generated.resources.home_cd_settings
import lexisoup.core.ui.strings.generated.resources.home_cd_switch_langs
import lexisoup.core.ui.strings.generated.resources.home_input_hint
import lexisoup.core.ui.strings.generated.resources.home_search_for_raw_input
import lexisoup.core.ui.strings.generated.resources.Res as ResStr

@OptIn(ExperimentalComposeUiApi::class)
@Suppress("LongParameterList")
@Composable
internal fun HomeScreen(
    state: HomeScreenState,
    onQueryChange: (query: String)->Unit,
    onLangsChange: (langFrom: Lang, langTo: Lang)->Unit,
    onSearch: SearchFn,
    onSuggestionClick: (Suggestion)->Unit,
    onLocalhostToggled: (Boolean)->Unit,
    onSettingsClick: ()->Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isSearchFocused by interactionSource.collectIsFocusedAsState()
    ClearSearchFocusOnBack(
        enabled = isSearchFocused || state.query.isNotEmpty(),
        onFocusCleared = {
            onQueryChange("")
        }
    )

    val onSearchWrapper = { query: String ->
        if (state.canSearch && state.langFrom != null && state.langTo != null) {
            onSearch(
                query.trim(),
                state.langFrom,
                state.langTo,
            )
        }
    }

    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
        ) {
            val biasY by animateFloatAsState(
                targetValue = if (isSearchFocused || state.query.isNotEmpty()) -1f else 0f,
            )
            Box(
                contentAlignment = BiasAlignment(horizontalBias = 0f, verticalBias = biasY),
                modifier = Modifier.fillMaxSize(),
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .wrapContentSize()
                        .padding(32.dp)
                ) {
                    var langsSelectorHeight by remember { mutableStateOf(0) }
                    if (state.langFrom != null && state.langTo != null) {
                        LangsSelector(
                            state.langFrom,
                            state.langTo,
                            onLangsChange,
                            modifier = Modifier.onGloballyPositioned { coords ->
                                langsSelectorHeight = coords.size.height
                            }
                        )
                    }

                    val focusRequester = remember { FocusRequester() }
                    SearchBar(
                        state.query,
                        onQueryChange = { onQueryChange(it) },
                        onSearch = { onSearchWrapper(state.query) },
                        placeholder = { Text(stringResource(ResStr.string.home_input_hint)) },
                        leadingIcon = {
                            SearchIcon()
                        },
                        trailingIcon = {
                            if (state.query.isNotEmpty()) {
                                IconButton(onClick = {
                                    onQueryChange("")
                                    focusRequester.requestFocus()
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Clear,
                                        contentDescription = stringResource(ResStr.string.home_cd_clear_search_query),
                                    )
                                }
                            }
                        },
                        interactionSource = interactionSource,
                        keyboardOptions = KeyboardOptions(
                            imeAction = ImeAction.Search,
                            hintLocales = state.langFrom?.iso2?.let { LocaleList(it) }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester),
                    )
                    Box {
                        val langsSelectorHeightDp =
                            with(LocalDensity.current) { langsSelectorHeight.toDp() }
                        Spacer(Modifier.height(langsSelectorHeightDp))
                        Column(modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                        ) {
                            val suggestions = state.suggestions
                            suggestions.forEach { suggestion ->
                                SuggestionItem(
                                    suggestion.text,
                                    suggestion.translations.map { it.text },
                                    Modifier.clickable {
                                        onSuggestionClick(suggestion)
                                    }
                                )
                            }
                            val suggestionsTarget = state.suggestionsTarget
                            if (state.canSearch
                                && suggestions.firstOrNull()?.text != suggestionsTarget
                                && suggestionsTarget.isNotBlank()
                            ) {
                                SuggestionItem(
                                    stringResource(
                                        ResStr.string.home_search_for_raw_input,
                                        suggestionsTarget,
                                        preview = "Search for \"Hund\"",
                                    ),
                                    emptyList(),
                                    Modifier.clickable {
                                        onSearchWrapper(suggestionsTarget)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (state.isLocalhost != null) {
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(32.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Localhost")
                    Spacer(Modifier.width(8.dp))
                    Switch(
                        checked = state.isLocalhost,
                        onCheckedChange = { isChecked -> onLocalhostToggled(isChecked) }
                    )
                }
            }

            IconButton(onClick = onSettingsClick) {
                Icon(
                    Icons.Filled.Settings,
                    contentDescription = stringResource(ResStr.string.home_cd_settings),
                )
            }
        }
    }
}

@Composable
private fun SuggestionItem(
    text: String,
    details: List<String>,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth()
    ) {
        Row(Modifier.padding(vertical = 6.dp)) {
            Text(if (details.isEmpty()) text else "$text: ")
            Text(
                details.joinToString(", ") { it },
                color = LocalContentColor.current.copy(alpha = 0.6f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun SearchIcon() {
    Icon(
        Icons.Default.Search,
        tint = LocalContentColor.current.copy(alpha = 0.4f),
        contentDescription = "",
    )
}

@Composable
private fun LangsSelector(
    langFrom: Lang,
    langTo: Lang,
    onLangsChange: (Lang, Lang) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        val langWidth = 100.dp
        LangDropdown(
            langFrom,
            textAlign = TextAlign.End,
            onSelected = { onLangsChange(it, langTo) },
            modifier = Modifier.width(langWidth)
        )
        IconButton(onClick = {
            onLangsChange(langTo, langFrom)
        }) {
            Icon(
                IconSwitch,
                contentDescription = stringResource(ResStr.string.home_cd_switch_langs),
            )
        }
        LangDropdown(
            langTo,
            textAlign = TextAlign.Start,
            onSelected = { onLangsChange(langFrom, it) },
            modifier = Modifier.width(langWidth)
        )
    }
}

@Preview(name = "400x500", heightDp = 400, widthDp = 500)
@Composable
private fun Preview() {
    val state = HomeScreenState(
        langFrom = Lang.DE,
        langTo = Lang.EN,
        query = "Hund",
        canSearch = true,
        suggestions = emptyList(),
        suggestionsTarget = "Hund",
        isLocalhost = false,
    )
    LexisoupTheme {
        HomeScreen(
            state = state,
            onQueryChange = {},
            onLangsChange = { _, _ -> },
            onSearch = { _, _, _ -> },
            onSuggestionClick = {},
            onLocalhostToggled = {},
            onSettingsClick = {},
        )
    }
}

@Preview(name = "400x500", heightDp = 400, widthDp = 500)
@Composable
private fun PreviewSuggestions() {
    val state = HomeScreenState(
        langFrom = Lang.DE,
        langTo = Lang.EN,
        query = "Hund",
        canSearch = true,
        suggestions = listOf(
            Suggestion("Hunde", Lang.DE, listOf(
                Sentence("Dog", Lang.EN, DataSource.PanLex),
                Sentence("Mutt", Lang.EN, DataSource.PanLex),
                Sentence("Hound", Lang.EN, DataSource.PanLex),
            )),
            Suggestion("Hündin", Lang.DE, listOf(
                Sentence("Dog", Lang.EN, DataSource.PanLex),
                Sentence("Bitch", Lang.EN, DataSource.PanLex),
            )),
        ),
        suggestionsTarget = "Hund",
        isLocalhost = false,
    )
    LexisoupTheme {
        HomeScreen(
            state = state,
            onQueryChange = {},
            onLangsChange = { _, _ -> },
            onSearch = { _, _, _ -> },
            onSuggestionClick = {},
            onLocalhostToggled = {},
            onSettingsClick = {},
        )
    }
}
