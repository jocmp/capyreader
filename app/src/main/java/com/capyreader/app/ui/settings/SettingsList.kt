package com.capyreader.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.components.LocalSnackbarHost
import com.capyreader.app.ui.isSinglePane
import com.capyreader.app.ui.settings.panels.SettingsPanel
import com.capyreader.app.ui.settings.registry.SettingsEnvironment
import com.capyreader.app.ui.settings.registry.SettingsRegistry
import com.capyreader.app.ui.settings.registry.SettingsSearchResult
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.accounts.Source

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsList(
    selected: SettingsPanel?,
    environment: SettingsEnvironment,
    query: String,
    onQueryChange: (String) -> Unit,
    onNavigate: (panel: SettingsPanel) -> Unit,
    onSelectResult: (result: SettingsSearchResult) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val scrollBehavior = pinnedScrollBehavior()
    val snackbarHost = remember { SnackbarHostState() }

    CompositionLocalProvider(LocalSnackbarHost provides snackbarHost) {
        val notificationsGate = rememberNotificationsGate(
            onNavigate = { onNavigate(SettingsPanel.Notifications) }
        )

        val navigate = { panel: SettingsPanel ->
            if (panel == SettingsPanel.Notifications) {
                notificationsGate.open()
            } else {
                onNavigate(panel)
            }
        }

        val selectResult = { result: SettingsSearchResult ->
            if (result.panel == SettingsPanel.Notifications) {
                notificationsGate.open()
            } else {
                onSelectResult(result)
            }
        }

        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = { SnackbarHost(hostState = snackbarHost) },
            topBar = {
                TopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(stringResource(R.string.settings_list_top_bar_title))
                    },
                    navigationIcon = {
                        IconButton(
                            onClick = onNavigateBack,
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = null
                            )
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
            ) {
                SettingsSearchField(
                    query = query,
                    onQueryChange = onQueryChange,
                )

                Column(
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                ) {
                    if (query.isBlank()) {
                        SettingsPanel.items.forEach { panel ->
                            PanelItem(
                                panel = panel,
                                selected = panel == selected,
                                enabled = panel != SettingsPanel.Notifications || notificationsGate.enabled,
                                onClick = { navigate(panel) },
                            )
                        }
                    } else {
                        SettingsSearchResults(
                            query = query,
                            environment = environment,
                            onSelect = selectResult,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingsSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
) {
    val focusManager = LocalFocusManager.current
    val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh

    TextField(
        value = query,
        onValueChange = onQueryChange,
        singleLine = true,
        shape = CircleShape,
        placeholder = {
            Text(stringResource(R.string.settings_search_placeholder))
        },
        leadingIcon = {
            Icon(Icons.Rounded.Search, contentDescription = null)
        },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Rounded.Close, contentDescription = null)
                }
            }
        },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = containerColor,
            unfocusedContainerColor = containerColor,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
        ),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    )
}

@Composable
private fun SettingsSearchResults(
    query: String,
    environment: SettingsEnvironment,
    onSelect: (SettingsSearchResult) -> Unit,
) {
    val resources = LocalResources.current
    val results = remember(query, environment, resources) {
        SettingsRegistry.search(query, environment) { resources.getString(it) }
    }

    if (results.isEmpty()) {
        Text(
            text = stringResource(R.string.settings_search_no_results),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
        )
        return
    }

    results.forEach { result ->
        SettingsListRow(onClick = { onSelect(result) }) {
            ListItem(
                leadingContent = {
                    Icon(result.panel.icon(), contentDescription = null)
                },
                headlineContent = {
                    Text(result.title)
                },
                supportingContent = if (result.path.isNotEmpty()) {
                    { Text(result.path.joinToString(" › ")) }
                } else {
                    null
                },
            )
        }
    }
}

@Composable
private fun PanelItem(
    panel: SettingsPanel,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val defaults = ListItemDefaults.colors()

    val containerColor = if (!isSinglePane() && selected) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (enabled) {
        defaults.contentColor
    } else {
        defaults.disabledContentColor
    }

    SettingsListRow(onClick = onClick) {
        ListItem(
            leadingContent = {
                Icon(panel.icon(), contentDescription = null)
            },
            colors = ListItemDefaults.colors(
                containerColor = containerColor,
                headlineColor = contentColor,
                leadingIconColor = contentColor,
            ),
            headlineContent = {
                Text(stringResource(panel.title))
            },
            supportingContent = if (enabled) {
                null
            } else {
                { Text(stringResource(R.string.settings_enable_refresh_call_to_action)) }
            },
        )
    }
}

@Composable
private fun SettingsListRow(
    onClick: () -> Unit,
    content: @Composable () -> Unit,
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
    ) {
        content()
    }
}

@Preview
@Composable
private fun SettingsListPreview() {
    CapyTheme {
        SettingsList(
            selected = SettingsPanel.Display,
            environment = SettingsEnvironment(
                source = Source.LOCAL,
                crashReporting = false,
                debug = false,
            ),
            query = "",
            onQueryChange = {},
            onNavigate = {},
            onSelectResult = {},
            onNavigateBack = {}
        )
    }
}
