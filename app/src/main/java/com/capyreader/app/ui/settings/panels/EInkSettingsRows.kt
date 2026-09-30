package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.capyreader.app.common.RowItem
import com.capyreader.app.ui.components.TextSwitch
import org.koin.androidx.compose.koinViewModel

@Composable
fun ReduceMotionRow(viewModel: GesturesSettingsViewModel = koinViewModel()) {
    RowItem {
        TextSwitch(
            onCheckedChange = viewModel::updateReduceMotion,
            checked = viewModel.reduceMotion,
            title = stringResource(R.string.settings_reduce_motion_title),
            subtitle = stringResource(R.string.settings_reduce_motion_subtitle)
        )
    }
}

@Composable
fun TapToPageRow(viewModel: GesturesSettingsViewModel = koinViewModel()) {
    RowItem {
        TextSwitch(
            onCheckedChange = viewModel::updatePagingTapGesture,
            checked = viewModel.enablePagingTapGesture,
            title = stringResource(R.string.settings_e_ink_tap_to_page_title),
            subtitle = stringResource(R.string.settings_e_ink_tap_to_page_subtitle)
        )
    }
}

@Composable
fun EInkScrollbarRow(viewModel: GesturesSettingsViewModel = koinViewModel()) {
    RowItem {
        TextSwitch(
            onCheckedChange = viewModel::updateEInkScrollbar,
            checked = viewModel.enableEInkScrollbar,
            title = stringResource(R.string.settings_e_ink_scrollbar_title),
            subtitle = stringResource(R.string.settings_e_ink_scrollbar_subtitle)
        )
    }
}

@Composable
fun PageTurnKeysRow(viewModel: GesturesSettingsViewModel = koinViewModel()) {
    RowItem {
        TextSwitch(
            onCheckedChange = viewModel::updatePageTurnKeys,
            checked = viewModel.enablePageTurnKeys,
            title = stringResource(R.string.settings_e_ink_page_turn_keys_title),
            subtitle = stringResource(R.string.settings_e_ink_page_turn_keys_subtitle)
        )
    }
}
