package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun ArticleListSettingsPanel(
    viewModel: DisplaySettingsViewModel = koinViewModel(),
) {
    ArticleListSettings(
        options = ArticleListOptions(
            imagePreview = viewModel.imagePreview,
            showSummary = viewModel.showSummary,
            fontScale = viewModel.fontScale,
            showFeedIcons = viewModel.showFeedIcons,
            showFeedName = viewModel.showFeedName,
            shortenTitles = viewModel.shortenTitles,
            updateImagePreview = viewModel::updateImagePreview,
            updateSummary = viewModel::updateSummary,
            updateFeedName = viewModel::updateFeedName,
            updateFeedIcons = viewModel::updateFeedIcons,
            updateFontScale = viewModel::updateFontScale,
            updateShortenTitles = viewModel::updateShortenTitles,
        )
    )
}
