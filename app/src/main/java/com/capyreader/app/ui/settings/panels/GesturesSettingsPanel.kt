package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.preferences.BackAction
import com.capyreader.app.common.RowItem
import com.capyreader.app.preferences.ArticleVerticalSwipe
import com.capyreader.app.preferences.RowSwipeOption
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.components.TextSwitch
import com.capyreader.app.ui.settings.PreferenceSelect
import com.capyreader.app.ui.theme.CapyTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun GesturesSettingPanel(
    viewModel: GesturesSettingsViewModel = koinViewModel(),
) {
    GesturesSettingsPanelView(
        updateBackAction = viewModel::updateBackAction,
        updatePagingTapGesture = viewModel::updatePagingTapGesture,
        updateReaderBottomSwipe = viewModel::updateReaderBottomSwipe,
        updateReaderTopSwipe = viewModel::updateReaderTopSwipe,
        updateRowSwipeEnd = viewModel::updateRowSwipeEnd,
        updateRowSwipeStart = viewModel::updateRowSwipeStart,
        updateHorizontalPagination = viewModel::updateHorizontalPagination,
        enableHorizontalPagination = viewModel.enableHorizontalPagination,
        backAction = viewModel.backAction,
        bottomSwipe = viewModel.readerBottomSwipe,
        enablePagingTapGesture = viewModel.enablePagingTapGesture,
        rowSwipeEnd = viewModel.rowSwipeEnd,
        rowSwipeStart = viewModel.rowSwipeStart,
        topSwipe = viewModel.readerTopSwipe,
    )
}

@Composable
private fun GesturesSettingsPanelView(
    updateBackAction: (BackAction) -> Unit,
    updatePagingTapGesture: (enabled: Boolean) -> Unit,
    updateReaderBottomSwipe: (swipe: ArticleVerticalSwipe) -> Unit,
    updateReaderTopSwipe: (swipe: ArticleVerticalSwipe) -> Unit,
    updateRowSwipeEnd: (swipe: RowSwipeOption) -> Unit,
    updateRowSwipeStart: (swipe: RowSwipeOption) -> Unit,
    updateHorizontalPagination: (enable: Boolean) -> Unit,
    enableHorizontalPagination: Boolean,
    backAction: BackAction,
    bottomSwipe: ArticleVerticalSwipe,
    enablePagingTapGesture: Boolean,
    rowSwipeEnd: RowSwipeOption,
    rowSwipeStart: RowSwipeOption,
    topSwipe: ArticleVerticalSwipe,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        FormSection(title = stringResource(R.string.settings_reader_title)) {
            Column {
                PreferenceSelect(
                    selected = topSwipe,
                    update = updateReaderTopSwipe,
                    options = ArticleVerticalSwipe.topOptions,
                    label = R.string.settings_gestures_reader_swipe_down,
                    disabledOption = ArticleVerticalSwipe.DISABLED,
                    optionText = {
                        stringResource(it.translationKey)
                    }
                )

                PreferenceSelect(
                    selected = bottomSwipe,
                    update = updateReaderBottomSwipe,
                    options = ArticleVerticalSwipe.bottomOptions,
                    label = R.string.settings_gestures_reader_swipe_up,
                    disabledOption = ArticleVerticalSwipe.DISABLED,
                    optionText = { stringResource(it.translationKey) }
                )

                RowItem {
                    TextSwitch(
                        onCheckedChange = updateHorizontalPagination,
                        checked = enableHorizontalPagination,
                        title = { Text(stringResource(R.string.settings_gestures_enable_horizontal_pagination_title)) },
                        subtitle = stringResource(R.string.settings_gestures_enable_horizontal_pagination_subtitle)
                    )
                }

                RowItem {
                    TextSwitch(
                        onCheckedChange = updatePagingTapGesture,
                        checked = enablePagingTapGesture,
                        title = { Text(stringResource(R.string.settings_gestures_reader_tap_to_page_title)) },
                        subtitle = stringResource(R.string.settings_gestures_reader_tap_to_page_subtitle)
                    )
                }
            }
        }

        FormSection(title = stringResource(R.string.settings_article_list_title)) {
            Column {
                PreferenceSelect(
                    selected = rowSwipeStart,
                    update = updateRowSwipeStart,
                    options = RowSwipeOption.sorted,
                    label = R.string.settings_gestures_list_row_swipe_start,
                    disabledOption = RowSwipeOption.DISABLED,
                    optionText = { stringResource(it.translationKey) }
                )

                PreferenceSelect(
                    selected = rowSwipeEnd,
                    update = updateRowSwipeEnd,
                    options = RowSwipeOption.sorted,
                    label = R.string.settings_gestures_list_row_swipe_end,
                    disabledOption = RowSwipeOption.DISABLED,
                    optionText = { stringResource(it.translationKey) }
                )
                PreferenceSelect(
                    selected = backAction,
                    update = updateBackAction,
                    options = BackAction.entries,
                    label = R.string.settings_gestures_list_back_navigation_action,
                    optionText = { stringResource(it.translationKey) }
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Preview
@Composable
fun GesturesSettingsPanelPreview() {
    CapyTheme {
        GesturesSettingsPanelView(
            updateBackAction = {},
            updateRowSwipeStart = {},
            updateRowSwipeEnd = {},
            updateReaderTopSwipe = {},
            updateReaderBottomSwipe = {},
            updatePagingTapGesture = {},
            updateHorizontalPagination = {},
            backAction = BackAction.OPEN_DRAWER,
            topSwipe = ArticleVerticalSwipe.PREVIOUS_ARTICLE,
            bottomSwipe = ArticleVerticalSwipe.NEXT_ARTICLE,
            rowSwipeStart = RowSwipeOption.TOGGLE_READ,
            rowSwipeEnd = RowSwipeOption.TOGGLE_STARRED,
            enablePagingTapGesture = true,
            enableHorizontalPagination = true,
        )
    }
}
