package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.R
import com.capyreader.app.ui.components.TextSwitch

@Immutable
data class ReaderOptions(
    val updateStickyFullContent: (enable: Boolean) -> Unit,
    val updatePinTopBar: (pin: Boolean) -> Unit,
    val enableStickyFullContent: Boolean,
    val pinTopBar: Boolean,
)

@Composable
fun ReaderSettings(
    options: ReaderOptions,
) {
    Column {
        TextSwitch(
            checked = options.enableStickyFullContent,
            onCheckedChange = options.updateStickyFullContent,
            title = stringResource(R.string.settings_option_full_content_title),
            subtitle = stringResource(R.string.settings_option_full_content_subtitle)
        )
        TextSwitch(
            checked = options.pinTopBar,
            onCheckedChange = options.updatePinTopBar,
            title = stringResource(R.string.settings_options_reader_pin_toolbars),
        )
    }
}

@Preview
@Composable
private fun ReaderSettingsPreview() {
    ReaderSettings(
        options = ReaderOptions(
            updateStickyFullContent = {},
            updatePinTopBar = {},
            enableStickyFullContent = true,
            pinTopBar = false
        )
    )
}
