package blazern.lexisoup.feature.home.ui

import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import blazern.lexisoup.core.ui.theme.LexisoupTheme
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> DropdownMenu(
    selected: T,
    options: List<T>,
    textAlign: TextAlign,
    i18n: @Composable (T)->String,
    onSelected: (T)->Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        Text(
            i18n(selected),
            textAlign = textAlign,
            modifier = modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(i18n(option), color = MaterialTheme.colorScheme.onSurface) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    },
                    contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    LexisoupTheme {
        DropdownMenu<String>(
            "Selected",
            listOf("Alternative 1", "Alternative 2", "Alternative 3"),
            textAlign = TextAlign.End,
            onSelected = { },
            i18n = { it },
        )
    }
}
