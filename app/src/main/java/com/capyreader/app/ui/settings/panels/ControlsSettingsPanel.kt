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
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.settings.KeyBindingPreference
import com.capyreader.app.ui.theme.CapyTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun ControlsSettingsPanel(
    viewModel: ControlsSettingsViewModel = koinViewModel(),
) {
    ControlsSettingsPanelView(
        scrollUpKeyCode = viewModel.scrollUpKeyCode,
        scrollDownKeyCode = viewModel.scrollDownKeyCode,
        previousArticleKeyCode = viewModel.previousArticleKeyCode,
        nextArticleKeyCode = viewModel.nextArticleKeyCode,
        toggleStarKeyCode = viewModel.toggleStarKeyCode,
        updateScrollUpKeyCode = viewModel::updateScrollUpKeyCode,
        updateScrollDownKeyCode = viewModel::updateScrollDownKeyCode,
        updatePreviousArticleKeyCode = viewModel::updatePreviousArticleKeyCode,
        updateNextArticleKeyCode = viewModel::updateNextArticleKeyCode,
        updateToggleStarKeyCode = viewModel::updateToggleStarKeyCode,
    )
}

@Composable
private fun ControlsSettingsPanelView(
    scrollUpKeyCode: Int,
    scrollDownKeyCode: Int,
    previousArticleKeyCode: Int,
    nextArticleKeyCode: Int,
    toggleStarKeyCode: Int,
    updateScrollUpKeyCode: (Int) -> Unit,
    updateScrollDownKeyCode: (Int) -> Unit,
    updatePreviousArticleKeyCode: (Int) -> Unit,
    updateNextArticleKeyCode: (Int) -> Unit,
    updateToggleStarKeyCode: (Int) -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.verticalScroll(rememberScrollState()),
    ) {
        FormSection(title = stringResource(R.string.settings_controls_reader_controls)) {
            Column {
                KeyBindingPreference(
                    keyCode = scrollUpKeyCode,
                    onKeyCodeChange = updateScrollUpKeyCode,
                    label = R.string.settings_controls_scroll_up,
                )

                KeyBindingPreference(
                    keyCode = scrollDownKeyCode,
                    onKeyCodeChange = updateScrollDownKeyCode,
                    label = R.string.settings_controls_scroll_down,
                )

                KeyBindingPreference(
                    keyCode = previousArticleKeyCode,
                    onKeyCodeChange = updatePreviousArticleKeyCode,
                    label = R.string.settings_controls_previous_article,
                )

                KeyBindingPreference(
                    keyCode = nextArticleKeyCode,
                    onKeyCodeChange = updateNextArticleKeyCode,
                    label = R.string.settings_controls_next_article,
                )

                KeyBindingPreference(
                    keyCode = toggleStarKeyCode,
                    onKeyCodeChange = updateToggleStarKeyCode,
                    label = R.string.settings_controls_toggle_star,
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Preview
@Composable
fun ControlsSettingsPanelPreview() {
    CapyTheme {
        ControlsSettingsPanelView(
            scrollUpKeyCode = 19,
            scrollDownKeyCode = 20,
            previousArticleKeyCode = 21,
            nextArticleKeyCode = 22,
            toggleStarKeyCode = 96,
            updateScrollUpKeyCode = {},
            updateScrollDownKeyCode = {},
            updatePreviousArticleKeyCode = {},
            updateNextArticleKeyCode = {},
            updateToggleStarKeyCode = {},
        )
    }
}
