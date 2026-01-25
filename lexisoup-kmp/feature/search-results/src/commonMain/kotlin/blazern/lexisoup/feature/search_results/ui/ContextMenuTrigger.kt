package blazern.lexisoup.feature.search_results.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@Composable
internal fun ContextMenuTrigger(
    actions: List<ContextAction>,
    content: @Composable () -> Unit,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.clickable {
            menuExpanded = true
        }
    ) {
        content()
        DropdownMenu(
            expanded = menuExpanded,
            onDismissRequest = { menuExpanded = false },
        ) {
            actions.forEach { action ->
                DropdownMenuItem(
                    text = { Text(action.title) },
                    onClick = {
                        menuExpanded = false
                        action.onClick()
                    }
                )
            }
        }
    }
}

internal data class ContextAction(
    val title: String,
    val onClick: () -> Unit,
)
