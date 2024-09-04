package com.capyreader.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.common.ThemeOption
import com.capyreader.app.ui.articles.ArticleListFontScale
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.components.TextSwitch
import com.capyreader.app.ui.theme.CapyTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun DisplaySettingsPanel(
    viewModel: DisplaySettingsViewModel = koinViewModel(),
) {
    DisplaySettingsPanelView(
        updateStickyFullContent = viewModel::updateStickyFullContent,
        enableStickyFullContent = viewModel.enableStickyFullContent,
        onUpdateTheme = viewModel::updateTheme,
        theme = viewModel.theme,
        articleListOptions = ArticleListOptions(
            imagePreview = viewModel.imagePreview,
            showSummary = viewModel.showSummary,
            fontScale = viewModel.fontScale,
            showFeedIcons = viewModel.showFeedIcons,
            showFeedName = viewModel.showFeedName,
            updateImagePreview = viewModel::updateImagePreview,
            updateSummary = viewModel::updateSummary,
            updateFeedName = viewModel::updateFeedName,
            updateFeedIcons = viewModel::updateFeedIcons,
            updateFontScale = viewModel::updateFontScale,
        )
    )
}

@Composable
fun DisplaySettingsPanelView(
    updateStickyFullContent: (Boolean) -> Unit,
    enableStickyFullContent: Boolean,
    onUpdateTheme: (theme: ThemeOption) -> Unit,
    theme: ThemeOption,
    articleListOptions: ArticleListOptions,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            RowItem {
                ThemeMenu(onUpdateTheme = onUpdateTheme, theme = theme)
            }

            RowItem {
                TextSwitch(
                    checked = enableStickyFullContent,
                    onCheckedChange = updateStickyFullContent,
                    title = stringResource(R.string.settings_option_full_content_title),
                    subtitle = stringResource(R.string.settings_option_full_content_subtitle)
                )
            }
        }

        FormSection(
            title = stringResource(R.string.settings_article_list_title)
        ) {
            RowItem {
                ArticleListSettings(
                    options = articleListOptions
                )
            }
        }
    }
}

@Preview
@Composable
private fun DisplaySettingsPanelViewPreview() {
    CapyTheme {
        DisplaySettingsPanelView(
            updateStickyFullContent = {},
            enableStickyFullContent = true,
            onUpdateTheme = {},
            theme = ThemeOption.SYSTEM_DEFAULT,
            articleListOptions = ArticleListOptions(
                imagePreview = ImagePreview.default,
                showSummary = true,
                fontScale = ArticleListFontScale.MEDIUM,
                showFeedIcons = true,
                showFeedName = false,
                updateImagePreview = {},
                updateSummary = {},
                updateFeedName = {},
                updateFeedIcons = {},
                updateFontScale = {}
            )
        )
    }
}
