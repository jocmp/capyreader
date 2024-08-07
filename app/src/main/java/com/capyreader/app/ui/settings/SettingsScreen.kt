package com.capyreader.app.ui.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.capyreader.app.common.GetOPMLContent
import com.capyreader.app.common.ImagePreview
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val picker = rememberLauncherForActivityResult(
        GetOPMLContent()
    ) { uri ->
        viewModel.startOPMLImport(uri = uri)
    }

    val importOPML = {
        picker.launch(listOf("text/xml", "text/x-opml", "application/*"))
    }

    val exportOPML = {
        coroutineScope.launch(Dispatchers.IO) {
            OPMLExporter(context = context).export(viewModel.account)
        }
    }

    SettingsView(
        onNavigateBack = { onNavigateBack() },
        onRequestRemoveAccount = {
            viewModel.removeAccount()
            onRemoveAccount()
        },
        onRequestExport = { exportOPML() },
        onRequestImport = {
            importOPML()
        },

        accountSource = viewModel.accountSource,
        accountName = viewModel.accountName,
        importProgress = viewModel.importProgress,
        settings = SettingsOptions(
            canOpenLinksInternally = viewModel.canOpenLinksInternally,
            updateOpenLinksInternally = viewModel::updateOpenLinksInternally,
            refreshInterval = viewModel.refreshInterval,
            updateRefreshInterval = viewModel::updateRefreshInterval,
            updateTheme = viewModel::updateTheme,
            theme = viewModel.theme,
            updateStickFullContent = viewModel::updateStickyFullContent,
            enableStickyFullContent = viewModel.enableStickyFullContent,
            articleList = ArticleListOptions(
                imagePreview = viewModel.imagePreview,
                showSummary = viewModel.showSummary,
                showFeedName = viewModel.showFeedName,
                showFeedIcons = viewModel.showFeedIcons,
                updateSummary = viewModel::updateSummary,
                updateFeedIcons = viewModel::updateFeedIcons,
                updateImagePreview = viewModel::updateImagePreview,
                updateFeedName = viewModel::updateFeedName,
            )
        )
    )
}
