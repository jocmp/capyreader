package com.capyreader.app.ui.settings

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.R
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.theme.CapyTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun GesturesSettingPanel(
    viewModel: GesturesSettingsViewModel = koinViewModel(),
) {
    GesturesSettingsPanelView(
        updateReaderTopSwipe = viewModel::updateReaderTopSwipe,
        updateReaderBottomSwipe = viewModel::updateReaderBottomSwipe,
        topSwipe = viewModel.topSwipe,
        bottomSwipe = viewModel.bottomSwipe,
    )
}

@Composable
private fun GesturesSettingsPanelView(
    updateReaderTopSwipe: (swipe: ArticleVerticalSwipe) -> Unit,
    updateReaderBottomSwipe: (swipe: ArticleVerticalSwipe) -> Unit,
    topSwipe: ArticleVerticalSwipe,
    bottomSwipe: ArticleVerticalSwipe,
) {
    FormSection(title = stringResource(R.string.settings_reader_title)) {
        RowItem {
            PreferenceDropdown(
                selected = topSwipe,
                update = updateReaderTopSwipe,
                options = ArticleVerticalSwipe.topOptions,
                label = R.string.settings_gestures_reader_swipe_down,
                disabledOption = ArticleVerticalSwipe.DISABLED,
            )
        }
        RowItem {
            PreferenceDropdown(
                selected = bottomSwipe,
                update = updateReaderBottomSwipe,
                options = ArticleVerticalSwipe.bottomOptions,
                label = R.string.settings_gestures_reader_swipe_up,
                disabledOption = ArticleVerticalSwipe.DISABLED,
            )
        }
    }
}

@Preview
@Composable
fun GesturesSettingsPanelPreview() {
    CapyTheme {
        GesturesSettingsPanelView(
            updateReaderTopSwipe = {},
            updateReaderBottomSwipe = {},
            topSwipe = ArticleVerticalSwipe.PREVIOUS_ARTICLE,
            bottomSwipe = ArticleVerticalSwipe.NEXT_ARTICLE
        )
    }
}
