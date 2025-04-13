package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.RowItem
import com.capyreader.app.preferences.ArticleListVerticalSwipe
import com.capyreader.app.preferences.ArticleVerticalSwipe
import com.capyreader.app.preferences.BackAction
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
        updateRowSwipeStart = viewModel::updateRowSwipeStart,
        updateRowSwipeEnd = viewModel::updateRowSwipeEnd,
        updateListSwipeBottom = viewModel::updateListSwipeBottom,
        updateHorizontalPagination = viewModel::updateHorizontalPagination,
        enableHorizontalPagination = viewModel.enableHorizontalPagination,
        backAction = viewModel.backAction,
        bottomSwipe = viewModel.readerBottomSwipe,
        enablePagingTapGesture = viewModel.enablePagingTapGesture,
        rowSwipeStart = viewModel.rowSwipeStart,
        rowSwipeEnd = viewModel.rowSwipeEnd,
        listSwipeBottom = viewModel.listSwipeBottom,
        topSwipe = viewModel.readerTopSwipe,
        updateImproveTalkback = viewModel::updateImproveTalkback,
        improveTalkback = viewModel.improveTalkback,
    )
}

@Composable
private fun GesturesSettingsPanelView(
    updateBackAction: (BackAction) -> Unit,
    updatePagingTapGesture: (enabled: Boolean) -> Unit,
    updateReaderBottomSwipe: (swipe: ArticleVerticalSwipe) -> Unit,
    updateReaderTopSwipe: (swipe: ArticleVerticalSwipe) -> Unit,
    updateRowSwipeStart: (swipe: RowSwipeOption) -> Unit,
    updateRowSwipeEnd: (swipe: RowSwipeOption) -> Unit,
    updateListSwipeBottom: (swipe: ArticleListVerticalSwipe) -> Unit,
    updateHorizontalPagination: (enable: Boolean) -> Unit,
    enableHorizontalPagination: Boolean,
    backAction: BackAction,
    bottomSwipe: ArticleVerticalSwipe,
    enablePagingTapGesture: Boolean,
    rowSwipeStart: RowSwipeOption,
    rowSwipeEnd: RowSwipeOption,
    listSwipeBottom: ArticleListVerticalSwipe,
    topSwipe: ArticleVerticalSwipe,
    updateImproveTalkback: (improve: Boolean) -> Unit,
    improveTalkback: Boolean
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
                    enabled = !improveTalkback,
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
                    enabled = !improveTalkback,
                    disabledOption = ArticleVerticalSwipe.DISABLED,
                    optionText = { stringResource(it.translationKey) }
                )

                RowItem {
                    TextSwitch(
                        onCheckedChange = updateHorizontalPagination,
                        checked = enableHorizontalPagination,
                        title = stringResource(R.string.settings_gestures_enable_horizontal_pagination_title),
                        subtitle = stringResource(R.string.settings_gestures_enable_horizontal_pagination_subtitle)
                    )
                }

                RowItem {
                    TextSwitch(
                        onCheckedChange = updatePagingTapGesture,
                        checked = enablePagingTapGesture,
                        title = stringResource(R.string.settings_gestures_reader_tap_to_page_title),
                        subtitle = stringResource(R.string.settings_gestures_reader_tap_to_page_subtitle)
                    )
                }

                RowItem {
                    TextSwitch(
                        onCheckedChange = updateImproveTalkback,
                        checked = improveTalkback,
                        title = stringResource(R.string.settings_gestures_improve_talkback_title),
                        subtitle = stringResource(R.string.settings_gestures_improve_talkback_subtitle)
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
                    selected = listSwipeBottom,
                    update = updateListSwipeBottom,
                    options = ArticleListVerticalSwipe.entries,
                    label = R.string.settings_gestures_list_swipe_up,
                    disabledOption = ArticleListVerticalSwipe.DISABLED,
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
            updateListSwipeBottom = {},
            backAction = BackAction.OPEN_DRAWER,
            topSwipe = ArticleVerticalSwipe.PREVIOUS_ARTICLE,
            bottomSwipe = ArticleVerticalSwipe.NEXT_ARTICLE,
            rowSwipeStart = RowSwipeOption.TOGGLE_READ,
            rowSwipeEnd = RowSwipeOption.TOGGLE_STARRED,
            enablePagingTapGesture = true,
            enableHorizontalPagination = true,
            listSwipeBottom = ArticleListVerticalSwipe.NEXT_FEED,
            improveTalkback = true,
            updateImproveTalkback = {}
        )
    }
}
