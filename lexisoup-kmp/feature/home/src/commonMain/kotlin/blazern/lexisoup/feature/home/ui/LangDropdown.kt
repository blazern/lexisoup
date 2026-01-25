package blazern.lexisoup.feature.home.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import blazern.lexisoup.core.ui.strings.stringResource
import blazern.lexisoup.core.ui.theme.LexisoupTheme
import blazern.lexisoup.domain.model.Lang
import blazern.lexisoup.domain.model.strRsc
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LangDropdown(
    selected: Lang,
    textAlign: TextAlign,
    onSelected: (Lang)->Unit,
    modifier: Modifier = Modifier,
) {
    DropdownMenu(
        selected = selected,
        options = Lang.entries.toList(),
        textAlign = textAlign,
        i18n = { stringResource(it.strRsc) },
        onSelected = onSelected,
        modifier = modifier,
    )
}

@Preview
@Composable
private fun Preview() {
    LexisoupTheme {
        LangDropdown(
            Lang.EN,
            textAlign = TextAlign.End,
            onSelected = { },
        )
    }
}
