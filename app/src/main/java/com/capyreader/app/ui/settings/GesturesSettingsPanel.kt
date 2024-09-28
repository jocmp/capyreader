package com.capyreader.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.theme.CapyTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun GesturesSettingPanel(
    viewModel: GesturesSettingsViewModel = koinViewModel(),
) {
    GesturesSettingsPanelView(
        updateRowSwipeStart = viewModel::updateRowSwipeStart,
        updateRowSwipeEnd = viewModel::updateRowSwipeEnd,
        updateReaderTopSwipe = viewModel::updateReaderTopSwipe,
        updateReaderBottomSwipe = viewModel::updateReaderBottomSwipe,
        topSwipe = viewModel.readerTopSwipe,
        bottomSwipe = viewModel.readerBottomSwipe,
        rowSwipeStart = viewModel.rowSwipeStart,
        rowSwipeEnd = viewModel.rowSwipeEnd
    )
}

@Composable
private fun GesturesSettingsPanelView(
    updateRowSwipeStart: (swipe: RowSwipeOption) -> Unit,
    updateRowSwipeEnd: (swipe: RowSwipeOption) -> Unit,
    updateReaderTopSwipe: (swipe: ArticleVerticalSwipe) -> Unit,
    updateReaderBottomSwipe: (swipe: ArticleVerticalSwipe) -> Unit,
    topSwipe: ArticleVerticalSwipe,
    bottomSwipe: ArticleVerticalSwipe,
    rowSwipeStart: RowSwipeOption,
    rowSwipeEnd: RowSwipeOption,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        FormSection(title = stringResource(R.string.settings_reader_title)) {
            RowItem {
                PreferenceDropdown(
                    selected = topSwipe,
                    update = updateReaderTopSwipe,
                    options = ArticleVerticalSwipe.topOptions,
                    label = R.string.settings_gestures_reader_swipe_down,
                    disabledOption = ArticleVerticalSwipe.DISABLED,
                    optionText = {
                        stringResource(it.translationKey)
                    }
                )
            }
            RowItem {
                PreferenceDropdown(
                    selected = bottomSwipe,
                    update = updateReaderBottomSwipe,
                    options = ArticleVerticalSwipe.bottomOptions,
                    label = R.string.settings_gestures_reader_swipe_up,
                    disabledOption = ArticleVerticalSwipe.DISABLED,
                    optionText = { stringResource(it.translationKey) }
                )
            }
        }
        FormSection(title = stringResource(R.string.settings_article_list_title)) {
            RowItem {
                PreferenceDropdown(
                    selected = rowSwipeStart,
                    update = updateRowSwipeStart,
                    options = RowSwipeOption.sorted,
                    label = R.string.settings_gestures_list_row_swipe_start,
                    disabledOption = RowSwipeOption.DISABLED,
                    optionText = { stringResource(it.translationKey) }
                )
            }
            RowItem {
                PreferenceDropdown(
                    selected = rowSwipeEnd,
                    update = updateRowSwipeEnd,
                    options = RowSwipeOption.sorted,
                    label = R.string.settings_gestures_list_row_swipe_end,
                    disabledOption = RowSwipeOption.DISABLED,
                    optionText = { stringResource(it.translationKey) }
                )
            }
        }
    }
}

@Preview
@Composable
fun GesturesSettingsPanelPreview() {
    CapyTheme {
        GesturesSettingsPanelView(
            updateRowSwipeStart = {},
            updateRowSwipeEnd = {},
            updateReaderTopSwipe = {},
            updateReaderBottomSwipe = {},
            topSwipe = ArticleVerticalSwipe.PREVIOUS_ARTICLE,
            bottomSwipe = ArticleVerticalSwipe.NEXT_ARTICLE,
            rowSwipeStart = RowSwipeOption.TOGGLE_READ,
            rowSwipeEnd = RowSwipeOption.TOGGLE_STARRED,
        )
    }
}
