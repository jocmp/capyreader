package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.common.RowItem
import com.capyreader.app.preferences.LayoutPreference
import com.capyreader.app.preferences.ReaderImageVisibility
import com.capyreader.app.preferences.ThemeOption
import com.capyreader.app.ui.articles.ArticleListFontScale
import com.capyreader.app.ui.articles.MarkReadPosition
import com.capyreader.app.ui.collectChangesWithCurrent
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.components.TextSwitch
import com.capyreader.app.ui.settings.PreferenceSelect
import com.capyreader.app.ui.theme.CapyTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun DisplaySettingsPanel(
    viewModel: DisplaySettingsViewModel = koinViewModel(),
) {
    val pinArticleBars by viewModel.pinArticleBars.collectChangesWithCurrent()
    val improveTalkback by viewModel.improveTalkback.collectChangesWithCurrent()
    val enableBottomBarActions by viewModel.enableBottomBarActions.collectChangesWithCurrent()
    val markReadButtonPosition by viewModel.markReadButtonPosition.collectChangesWithCurrent()

    DisplaySettingsPanelView(
        onUpdateTheme = viewModel::updateTheme,
        theme = viewModel.theme,
        enableHighContrastDarkTheme = viewModel.enableHighContrastDarkTheme,
        updateHighContrastDarkTheme = viewModel::updateHighContrastDarkTheme,
        updatePinArticleBars = viewModel::updatePinArticleBars,
        updateBottomBarActions = viewModel::updateBottomBarActions,
        enableBottomBarActions = enableBottomBarActions,
        pinArticleBars = pinArticleBars,
        enablePinArticleBars = !improveTalkback,
        updateImageVisibility = viewModel::updateImageVisibility,
        imageVisibility = viewModel.imageVisibility,
        layout = viewModel.layout,
        updateLayoutPreference = viewModel::updateLayoutPreference,
        markReadButtonPosition = markReadButtonPosition,
        updateMarkReadButtonPosition = viewModel::updateMarkReadButtonPosition,
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
    onUpdateTheme: (theme: ThemeOption) -> Unit,
    enableHighContrastDarkTheme: Boolean,
    updateHighContrastDarkTheme: (enabled: Boolean) -> Unit,
    updatePinArticleBars: (enable: Boolean) -> Unit,
    updateBottomBarActions: (enable: Boolean) -> Unit,
    pinArticleBars: Boolean,
    enableBottomBarActions: Boolean,
    enablePinArticleBars: Boolean,
    imageVisibility: ReaderImageVisibility,
    layout: LayoutPreference,
    markReadButtonPosition: MarkReadPosition,
    updateLayoutPreference: (layout: LayoutPreference) -> Unit,
    updateImageVisibility: (option: ReaderImageVisibility) -> Unit,
    updateMarkReadButtonPosition: (position: MarkReadPosition) -> Unit,
    theme: ThemeOption,
    articleListOptions: ArticleListOptions,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        Column {
            PreferenceSelect(
                selected = theme,
                update = onUpdateTheme,
                options = ThemeOption.sorted,
                label = R.string.theme_menu_label,
                optionText = {
                    stringResource(it.translationKey)
                }
            )

            RowItem {
                TextSwitch(
                    onCheckedChange = updateHighContrastDarkTheme,
                    checked = enableHighContrastDarkTheme,
                    title = stringResource(R.string.settings_enable_high_contrast_dark_theme)
                )
            }
        }
        FormSection(
            title = stringResource(R.string.settings_reader_title)
        ) {
            PreferenceSelect(
                selected = imageVisibility,
                update = updateImageVisibility,
                options = ReaderImageVisibility.entries,
                label = R.string.reader_image_visibility_label,
                optionText = {
                    stringResource(it.translationKey)
                }
            )
            RowItem {
                TextSwitch(
                    enabled = enablePinArticleBars,
                    checked = pinArticleBars,
                    onCheckedChange = updatePinArticleBars,
                    title = stringResource(R.string.settings_options_reader_pin_top_toolbar),
                )
            }
            RowItem {
                TextSwitch(
                    checked = enableBottomBarActions,
                    onCheckedChange = updateBottomBarActions,
                    title = stringResource(R.string.settings_options_reader_show_bottom_toolbar),
                )
            }
        }

        FormSection(
            title = stringResource(R.string.settings_article_list_title)
        ) {

            ArticleListSettings(
                options = articleListOptions
            )
        }

        FormSection(title = stringResource(R.string.settings_display_miscellaneous_title)) {
            PreferenceSelect(
                selected = layout,
                update = updateLayoutPreference,
                options = LayoutPreference.entries,
                label = R.string.layout_preference_label,
                optionText = {
                    stringResource(it.translationKey)
                }
            )
            PreferenceSelect(
                selected = markReadButtonPosition,
                update = updateMarkReadButtonPosition,
                options = MarkReadPosition.entries,
                label = R.string.mark_all_read_button_position,
                optionText = {
                    stringResource(it.translationKey)
                }
            )
        }

        Spacer(Modifier.height(16.dp))
    }
}

@Preview
@Composable
private fun DisplaySettingsPanelViewPreview() {
    CapyTheme {
        DisplaySettingsPanelView(
            onUpdateTheme = {},
            theme = ThemeOption.SYSTEM_DEFAULT,
            enableHighContrastDarkTheme = true,
            updateHighContrastDarkTheme = {},
            layout = LayoutPreference.RESPONSIVE,
            updateLayoutPreference = {},
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
                updateFontScale = {},
            ),
            updatePinArticleBars = {},
            pinArticleBars = false,
            updateImageVisibility = {},
            updateBottomBarActions = {},
            imageVisibility = ReaderImageVisibility.ALWAYS_SHOW,
            enablePinArticleBars = false,
            enableBottomBarActions = false,
            markReadButtonPosition = MarkReadPosition.TOOLBAR,
            updateMarkReadButtonPosition = {}
        )
    }
}
