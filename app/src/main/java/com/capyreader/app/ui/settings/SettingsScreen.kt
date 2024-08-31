package com.capyreader.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.navigation.rememberListDetailPaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.capyreader.app.common.GetOPMLContent
import com.capyreader.app.transfers.OPMLExporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = koinViewModel(),
    onRemoveAccount: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    SettingsView(
        onNavigateBack = onNavigateBack,
        onRemoveAccount = onRemoveAccount,
    )
}
