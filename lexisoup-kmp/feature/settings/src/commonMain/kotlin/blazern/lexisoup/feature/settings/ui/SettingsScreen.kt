package blazern.lexisoup.feature.settings.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import blazern.lexisoup.core.ui.strings.stringResource
import blazern.lexisoup.core.ui.theme.LinkColor
import blazern.lexisoup.domain.model.DataSource
import blazern.lexisoup.domain.model.LexicalItemDetail
import blazern.lexisoup.domain.model.i18n
import blazern.lexisoup.domain.model.predefined
import blazern.lexisoup.domain.model.strRsc
import blazern.lexisoup.domain.settings.SettingsRepository
import kotlinx.coroutines.launch
import lexisoup.core.ui.strings.generated.resources.Res
import lexisoup.core.ui.strings.generated.resources.search_results_title
import lexisoup.core.ui.strings.generated.resources.settings_data_sources_title
import lexisoup.core.ui.strings.generated.resources.settings_lexical_items_details_types_title
import lexisoup.core.ui.strings.generated.resources.settings_privacy_policy
import lexisoup.core.ui.strings.generated.resources.settings_title

@Composable
fun SettingsScreen(
    onPrivacyPolicyClick: () -> Unit,
    settingsRepo: SettingsRepository,
) {
    val excludedDataSourceIds by settingsRepo
        .getExcludedDataSourcesIDs()
        .collectAsState(initial = emptySet())

    val excludedDetailTypes by settingsRepo
        .getExcludedLexicalItemsDetailsTypes()
        .collectAsState(initial = emptySet())

    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier
            .statusBarsPadding()
            .fillMaxSize()
            .padding(horizontal = 16.dp),
    ) {
        item {
            Text(
                text = stringResource(Res.string.settings_title),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }

        item {
            SettingsSectionHeader(
                title = stringResource(Res.string.settings_data_sources_title),
            )
        }

        items(DataSource.predefined, key = { it.id }) { dataSource ->
            val isEnabled = dataSource.id !in excludedDataSourceIds

            SettingsCheckboxRow(
                title = dataSource.i18n(),
                checked = isEnabled,
                onCheckedChange = { shouldBeEnabled ->
                    val newExcludedIds = excludedDataSourceIds.toMutableSet().apply {
                        if (shouldBeEnabled) {
                            remove(dataSource.id)
                        } else {
                            add(dataSource.id)
                        }
                    }
                    coroutineScope.launch {
                        settingsRepo.setExcludedDataSourcesIDs(newExcludedIds)
                    }
                },
            )
        }

        item {
            SettingsSectionHeader(
                title = stringResource(Res.string.settings_lexical_items_details_types_title),
            )
        }

        items(LexicalItemDetail.Type.entries, key = { it.name }) { type ->
            val isEnabled = type !in excludedDetailTypes

            SettingsCheckboxRow(
                title = stringResource(type.strRsc),
                checked = isEnabled,
                onCheckedChange = { shouldBeEnabled ->
                    val newExcludedTypes = excludedDetailTypes.toMutableSet().apply {
                        if (shouldBeEnabled) {
                            remove(type)
                        } else {
                            add(type)
                        }
                    }
                    coroutineScope.launch {
                        settingsRepo.setExcludedLexicalItemsDetailsTypes(newExcludedTypes)
                    }
                },
            )
        }

        item {
            HorizontalDivider()
            Text(
                text = stringResource(Res.string.settings_privacy_policy),
                color = LinkColor,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier
                    .clickable { onPrivacyPolicyClick() }
                    .padding(vertical = 12.dp)
            )
        }

        item {
            Spacer(Modifier.navigationBarsPadding())
        }
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp),
    ) {
        HorizontalDivider()
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = 12.dp),
        )
    }
}

@Composable
private fun SettingsCheckboxRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .toggleable(
                value = checked,
                onValueChange = onCheckedChange,
            )
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = null,
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
