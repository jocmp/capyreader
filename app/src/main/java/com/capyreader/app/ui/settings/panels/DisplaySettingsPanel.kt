package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MultiChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.common.RowItem
import com.capyreader.app.preferences.AppTheme
import com.capyreader.app.preferences.LayoutPreference
import com.capyreader.app.preferences.ReaderImageVisibility
import com.capyreader.app.preferences.ThemeMode
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
        onUpdateThemeMode = viewModel::updateThemeMode,
        themeMode = viewModel.themeMode,
        pureBlackDarkMode = viewModel.pureBlackDarkMode,
        updatePureBlackDarkMode = viewModel::updatePureBlackDarkMode,
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
    onUpdateThemeMode: (themeMode: ThemeMode) -> Unit,
    themeMode: ThemeMode,
    pureBlackDarkMode: Boolean,
    updatePureBlackDarkMode: (enabled: Boolean) -> Unit,
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
    articleListOptions: ArticleListOptions,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.verticalScroll(rememberScrollState())
    ) {
        FormSection(
            title = stringResource(R.string.theme_menu_label)
        ) {
            Column {
                val options = ThemeMode.entries
                MultiChoiceSegmentedButtonRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    options.onEachIndexed { index, mode ->
                        SegmentedButton(
                            checked = themeMode == mode,
                            onCheckedChange = { onUpdateThemeMode(mode) },
                            shape = SegmentedButtonDefaults.itemShape(
                                index,
                                options.size,
                            ),
                        ) {
                            Text(stringResource(mode.translationKey))
                        }
                    }
                }

                RowItem {
                    TextSwitch(
                        onCheckedChange = updatePureBlackDarkMode,
                        checked = pureBlackDarkMode,
                        title = stringResource(R.string.settings_pure_black_dark_mode)
                    )
                }
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


@PreviewLightDark
@Preview
@Composable
private fun DisplaySettingsPanelViewPreview() {
    CapyTheme(appTheme = AppTheme.NEWSPRINT, pureBlack = true) {
        Surface {
            DisplaySettingsPanelView(
                onUpdateThemeMode = {},
                themeMode = ThemeMode.SYSTEM,
                pureBlackDarkMode = false,
                updatePureBlackDarkMode = {},
                layout = LayoutPreference.RESPONSIVE,
                updateLayoutPreference = {},
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
                updateBottomBarActions = {},
                imageVisibility = ReaderImageVisibility.ALWAYS_SHOW,
                enablePinArticleBars = false,
                enableBottomBarActions = false,
                markReadButtonPosition = MarkReadPosition.TOOLBAR,
                updateMarkReadButtonPosition = {}
            )
        }
    }
}
