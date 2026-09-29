package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.common.RowItem
import com.capyreader.app.preferences.ArticleVerticalSwipe
import com.capyreader.app.preferences.ReaderImageVisibility
import com.capyreader.app.ui.articles.detail.ArticleStylePicker
import com.capyreader.app.ui.collectChangesWithCurrent
import com.capyreader.app.ui.components.TextSwitch
import com.capyreader.app.ui.settings.PreferenceSelect
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReaderStyleRow() {
    ArticleStylePicker()
}

@Composable
fun ReaderImagesRow(viewModel: DisplaySettingsViewModel = koinViewModel()) {
    PreferenceSelect(
        selected = viewModel.imageVisibility,
        update = viewModel::updateImageVisibility,
        options = ReaderImageVisibility.entries,
        label = R.string.reader_image_visibility_label,
        optionText = {
            stringResource(it.translationKey)
        }
    )
}

@Composable
fun StickyFullContentRow(viewModel: GeneralSettingsViewModel = koinViewModel()) {
    RowItem {
        TextSwitch(
            checked = viewModel.enableStickyFullContent,
            onCheckedChange = viewModel::updateStickyFullContent,
            title = stringResource(R.string.settings_option_full_content_title),
            subtitle = stringResource(R.string.settings_option_full_content_subtitle)
        )
    }
}

@Composable
fun PinToolbarsRow(viewModel: DisplaySettingsViewModel = koinViewModel()) {
    val pinArticleBars by viewModel.pinArticleBars.collectChangesWithCurrent()

    RowItem {
        TextSwitch(
            checked = pinArticleBars,
            onCheckedChange = viewModel::updatePinArticleBars,
            title = stringResource(R.string.settings_options_reader_pin_top_toolbar),
        )
    }
}

@Composable
fun InAppBrowserRow(viewModel: GeneralSettingsViewModel = koinViewModel()) {
    RowItem {
        TextSwitch(
            checked = viewModel.canOpenLinksInternally,
            onCheckedChange = viewModel::updateOpenLinksInternally,
            title = stringResource(R.string.settings_option_in_app_browser)
        )
    }
}

@Composable
fun ReaderSwipeDownRow(viewModel: GesturesSettingsViewModel = koinViewModel()) {
    PreferenceSelect(
        selected = viewModel.readerTopSwipe,
        update = viewModel::updateReaderTopSwipe,
        options = ArticleVerticalSwipe.topOptions,
        label = R.string.settings_gestures_reader_swipe_down,
        disabledOption = ArticleVerticalSwipe.DISABLED,
        optionText = { stringResource(it.translationKey) }
    )
}

@Composable
fun ReaderSwipeUpRow(viewModel: GesturesSettingsViewModel = koinViewModel()) {
    PreferenceSelect(
        selected = viewModel.readerBottomSwipe,
        update = viewModel::updateReaderBottomSwipe,
        options = ArticleVerticalSwipe.bottomOptions,
        label = R.string.settings_gestures_reader_swipe_up,
        disabledOption = ArticleVerticalSwipe.DISABLED,
        optionText = { stringResource(it.translationKey) }
    )
}

@Composable
fun HorizontalPaginationRow(viewModel: GesturesSettingsViewModel = koinViewModel()) {
    RowItem {
        TextSwitch(
            onCheckedChange = viewModel::updateHorizontalPagination,
            checked = viewModel.enableHorizontalPagination,
            title = stringResource(R.string.settings_gestures_enable_horizontal_pagination_title),
            subtitle = stringResource(R.string.settings_gestures_enable_horizontal_pagination_subtitle)
        )
    }
}
