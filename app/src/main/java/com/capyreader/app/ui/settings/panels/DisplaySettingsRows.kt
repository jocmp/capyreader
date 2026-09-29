package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Text
import androidx.compose.material3.ToggleButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.RowItem
import com.capyreader.app.preferences.ThemeMode
import com.capyreader.app.ui.collectChangesWithCurrent
import com.capyreader.app.ui.components.TextSwitch
import com.capyreader.app.ui.components.ThemePicker
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ThemeModeRow(viewModel: DisplaySettingsViewModel = koinViewModel()) {
    val options = ThemeMode.entries

    RowItem {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(ButtonGroupDefaults.ConnectedSpaceBetween),
        ) {
            options.forEachIndexed { index, mode ->
                ToggleButton(
                    checked = viewModel.themeMode == mode,
                    onCheckedChange = { viewModel.updateThemeMode(mode) },
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
}

@Composable
fun ThemeRow(viewModel: DisplaySettingsViewModel = koinViewModel()) {
    ThemePicker(appPreferences = viewModel.appPreferences)
}

@Composable
fun PureBlackRow(viewModel: DisplaySettingsViewModel = koinViewModel()) {
    RowItem {
        TextSwitch(
            onCheckedChange = viewModel::updatePureBlackDarkMode,
            checked = viewModel.pureBlackDarkMode,
            title = stringResource(R.string.settings_pure_black_dark_mode)
        )
    }
}

@Composable
fun AccentColorsRow(viewModel: DisplaySettingsViewModel = koinViewModel()) {
    val appTheme by viewModel.appPreferences.appTheme.collectChangesWithCurrent()

    if (!appTheme.supportsFeedAccentColor) {
        return
    }

    RowItem {
        TextSwitch(
            onCheckedChange = viewModel::updateAccentColors,
            checked = viewModel.accentColors,
            title = stringResource(R.string.settings_accent_colors)
        )
    }
}
