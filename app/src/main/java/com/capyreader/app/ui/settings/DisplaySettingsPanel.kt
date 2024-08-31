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
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.components.TextSwitch
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun DisplaySettingsPanel(

) {
    DisplaySettingsPanelView(
        updateStickFullContent = {},
        enableStickyFullContent = true,
        onUpdateTheme = {},
        theme = ThemeOption.SYSTEM_DEFAULT,
        articleListOptions = ArticleListOptions(
            imagePreview = ImagePreview.default,
            showSummary = true,
            showFeedIcons = true,
            showFeedName = false,
            updateImagePreview = {},
            updateSummary = {},
            updateFeedName = {},
            updateFeedIcons = {},
        )
    )
}

@Composable
fun DisplaySettingsPanelView(
    updateStickFullContent: (Boolean) -> Unit,
    enableStickyFullContent: Boolean,
    onUpdateTheme: (theme: ThemeOption) -> Unit,
    theme: ThemeOption,
    articleListOptions: ArticleListOptions,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            RowItem {
                ThemeMenu(onUpdateTheme = onUpdateTheme, theme = theme)
            }

            RowItem {
                TextSwitch(
                    checked = enableStickyFullContent,
                    onCheckedChange = updateStickFullContent,
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
            updateStickFullContent = {},
            enableStickyFullContent = true,
            onUpdateTheme = {},
            theme = ThemeOption.SYSTEM_DEFAULT,
            articleListOptions = ArticleListOptions(
                imagePreview = ImagePreview.default,
                showSummary = true,
                showFeedIcons = true,
                showFeedName = false,
                updateImagePreview = {},
                updateSummary = {},
                updateFeedName = {},
                updateFeedIcons = {},
            )
        )
    }
}
