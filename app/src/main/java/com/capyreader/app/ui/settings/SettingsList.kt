package com.capyreader.app.ui.settings

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.isAtMostMedium
import com.capyreader.app.ui.theme.CapyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsList(
    selected: SettingsPanel?,
    onNavigate: (panel: SettingsPanel) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val scrollBehavior = pinnedScrollBehavior()
    val items = remember { SettingsPanel.items }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
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
            Column(
                modifier = Modifier
                    .padding(vertical = 16.dp)
            ) {
                items.forEach { panel ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                onNavigate(panel)
                            }
                    ) {
                        ListItem(
                            leadingContent = {
                                Icon(panel.icon(), contentDescription = null)
                            },
                            colors = ListItemDefaults.colors(
                                containerColor = if (!isAtMostMedium() && panel == selected) {
                                    MaterialTheme.colorScheme.surfaceContainerHigh
                                } else {
                                    MaterialTheme.colorScheme.surface
                                }
                            ),
                            headlineContent = {
                                Text(stringResource(panel.title))
                            },
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun SettingsListPreview() {
    CapyTheme {
        SettingsList(
            selected = SettingsPanel.Display,
            onNavigate = {},
            onNavigateBack = {}
        )
    }
}
