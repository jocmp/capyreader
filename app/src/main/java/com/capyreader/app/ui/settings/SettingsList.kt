package com.capyreader.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
<<<<<<< Updated upstream
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
=======
import androidx.compose.foundation.layout.padding
>>>>>>> Stashed changes
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
<<<<<<< Updated upstream
import androidx.compose.material3.LargeTopAppBar
=======
>>>>>>> Stashed changes
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
<<<<<<< Updated upstream
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
=======
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.pinnedScrollBehavior
>>>>>>> Stashed changes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
<<<<<<< Updated upstream
=======
import com.capyreader.app.ui.isCompact
>>>>>>> Stashed changes
import com.capyreader.app.ui.theme.CapyTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsList(
    selected: SettingsPanel?,
    onNavigate: (panel: SettingsPanel) -> Unit,
    onNavigateBack: () -> Unit,
) {
<<<<<<< Updated upstream
    val scrollBehavior = enterAlwaysScrollBehavior()
=======
    val scrollBehavior = pinnedScrollBehavior()
>>>>>>> Stashed changes
    val items = remember { SettingsPanel.items }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
<<<<<<< Updated upstream
            LargeTopAppBar(
=======
            TopAppBar(
>>>>>>> Stashed changes
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
<<<<<<< Updated upstream
                .wrapContentHeight()
=======
>>>>>>> Stashed changes
        ) {
            Column(
                modifier = Modifier
                    .padding(vertical = 16.dp)
<<<<<<< Updated upstream
                    .wrapContentHeight()
=======
>>>>>>> Stashed changes
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
                            colors = ListItemDefaults.colors(
<<<<<<< Updated upstream
                                containerColor = if (panel == selected) {
=======
                                containerColor = if (!isCompact() && panel == selected) {
>>>>>>> Stashed changes
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
