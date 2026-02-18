package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ListItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.common.RowItem
import com.capyreader.app.preferences.AppTheme
import com.capyreader.app.preferences.ReaderImageVisibility
import com.capyreader.app.preferences.ThemeMode
import com.capyreader.app.ui.articles.ArticleListFontScale
import com.capyreader.app.ui.articles.MarkReadPosition
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.components.TextSwitch
import com.capyreader.app.ui.components.ThemePicker
import com.capyreader.app.ui.settings.PreferenceSelect
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun DisplaySettingsPanel(
    viewModel: DisplaySettingsViewModel,
    onNavigateToUnreadBadges: () -> Unit = {},
) {
    DisplaySettingsPanelView(
        themeMode = viewModel.themeMode,
        updateThemeMode = viewModel::updateThemeMode,
        appTheme = viewModel.appTheme,
        updateAppTheme = viewModel::updateAppTheme,
        pureBlackDarkMode = viewModel.pureBlackDarkMode,
        updatePureBlackDarkMode = viewModel::updatePureBlackDarkMode,
        updatePinArticleBars = viewModel::updatePinArticleBars,
        pinArticleBars = viewModel.pinArticleBars,
        enablePinArticleBars = !viewModel.improveTalkback,
        updateImageVisibility = viewModel::updateImageVisibility,
        imageVisibility = viewModel.imageVisibility,
        markReadButtonPosition = viewModel.markReadButtonPosition,
        updateMarkReadButtonPosition = viewModel::updateMarkReadButtonPosition,
        onNavigateToUnreadBadges = onNavigateToUnreadBadges,
        articleListOptions = ArticleListOptions(
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

@Composable
fun DisplaySettingsPanelView(
    themeMode: ThemeMode,
    updateThemeMode: (ThemeMode) -> Unit,
    appTheme: AppTheme,
    updateAppTheme: (AppTheme) -> Unit,
    pureBlackDarkMode: Boolean,
    updatePureBlackDarkMode: (Boolean) -> Unit,
    updatePinArticleBars: (enable: Boolean) -> Unit,
    pinArticleBars: Boolean,
    enablePinArticleBars: Boolean,
    imageVisibility: ReaderImageVisibility,
    markReadButtonPosition: MarkReadPosition,
    updateImageVisibility: (option: ReaderImageVisibility) -> Unit,
    updateMarkReadButtonPosition: (position: MarkReadPosition) -> Unit,
    onNavigateToUnreadBadges: () -> Unit = {},
    articleListOptions: ArticleListOptions,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        FormSection(
            title = stringResource(R.string.theme_menu_label)
        ) {
            RowItem {
                ThemeModeButtons(
                    themeMode = themeMode,
                    updateThemeMode = updateThemeMode
                )
            }

            ThemePicker(
                currentTheme = appTheme,
                pureBlackDarkMode = pureBlackDarkMode,
                themeMode = themeMode,
                onSelectTheme = updateAppTheme,
            )

            RowItem {
                TextSwitch(
                    onCheckedChange = updatePureBlackDarkMode,
                    checked = pureBlackDarkMode,
                    title = stringResource(R.string.settings_pure_black_dark_mode)
                )
            }
            Box(Modifier.clickable { onNavigateToUnreadBadges() }) {
                ListItem(
                    headlineContent = {
                        Text(stringResource(R.string.settings_panel_unread_counts_title))
                    }
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

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun ThemeModeButtons(
    themeMode: ThemeMode,
    updateThemeMode: (ThemeMode) -> Unit
) {
    val options = ThemeMode.entries
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
    ) {
        options.forEachIndexed { index, mode ->
            ToggleButton(
                checked = themeMode == mode,
                onCheckedChange = { updateThemeMode(mode) },
                modifier = Modifier.weight(1f),
                shapes = when (index) {
                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                    options.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                }
            ) {
                Text(stringResource(mode.translationKey))
            }
        }
    }
}

@PreviewLightDark
@Preview
@Composable
private fun DisplaySettingsPanelViewPreview() {
    CapyTheme {
        Surface {
            DisplaySettingsPanelView(
                themeMode = ThemeMode.SYSTEM,
                updateThemeMode = {},
                appTheme = AppTheme.DEFAULT,
                updateAppTheme = {},
                pureBlackDarkMode = false,
                updatePureBlackDarkMode = {},
                articleListOptions = ArticleListOptions(
                    imagePreview = ImagePreview.default,
                    showSummary = true,
                    fontScale = ArticleListFontScale.MEDIUM,
                    showFeedIcons = true,
                    showFeedName = false,
                    shortenTitles = true,
                    updateImagePreview = {},
                    updateSummary = {},
                    updateFeedName = {},
                    updateFeedIcons = {},
                    updateFontScale = {},
                    updateShortenTitles = {},
                ),
                updatePinArticleBars = {},
                pinArticleBars = false,
                updateImageVisibility = {},
                imageVisibility = ReaderImageVisibility.ALWAYS_SHOW,
                enablePinArticleBars = false,
                markReadButtonPosition = MarkReadPosition.TOOLBAR,
                updateMarkReadButtonPosition = {}
            )
        }
    }
}
