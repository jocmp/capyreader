package com.capyreader.app.ui.settings

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults.exitUntilCollapsedScrollBehavior
import androidx.compose.material3.TopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.isAtMostMedium
import com.capyreader.app.ui.isExpanded
import com.capyreader.app.ui.settings.panels.SettingsPanel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPanelScaffold(
    panel: SettingsPanel,
    onBack: () -> Unit,
    content: @Composable () -> Unit,
) {
    val topBarState = rememberSaveable(panel, saver = TopAppBarState.Saver) {
        TopAppBarState(
            initialHeightOffsetLimit = 0f,
            initialHeightOffset = 0f,
            initialContentOffset = 0f,
        )
    }

    val scrollBehavior = exitUntilCollapsedScrollBehavior(state = topBarState)
    val snackbarHost = remember { SnackbarHostState() }

    CompositionLocalProvider(
        LocalSnackbarHost provides snackbarHost
    ) {
        Scaffold(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            snackbarHost = { SnackbarHost(hostState = snackbarHost) },
            topBar = {
                LargeTopAppBar(
                    scrollBehavior = scrollBehavior,
                    title = {
                        Text(stringResource(panel.title))
                    },
                    navigationIcon = {
                        if (isAtMostMedium() || panel.isNested()) {
                            IconButton(
                                onClick = {
                                    onBack()
                                }
                            ) {
                                Icon(
                                    imageVector = if (isExpanded() && panel.isNested()) {
                                        Icons.Rounded.Close
                                    } else {
                                        Icons.AutoMirrored.Rounded.ArrowBack
                                    },
                                    contentDescription = null
                                )
                            }
                        }
                    }
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .padding(padding)
            ) {
                content()
            }
        }
    }
}
