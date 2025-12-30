package com.capyreader.app.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.AppTheme
import com.capyreader.app.ui.collectChangesWithCurrent
import com.capyreader.app.ui.theme.CapyTheme
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemePicker(
    appPreferences: AppPreferences = koinInject(),
    onChange: () -> Unit = {}
) {
    val currentTheme by appPreferences.appTheme.collectChangesWithCurrent()
    val pureBlackDarkMode by appPreferences.pureBlackDarkMode.collectChangesWithCurrent()
    val themeMode by appPreferences.themeMode.collectChangesWithCurrent()

    val appThemes = remember {
        AppTheme.entries
            .filterNot { it == AppTheme.MONET && Build.VERSION.SDK_INT < Build.VERSION_CODES.S }
    }

    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        OutlinedTextField(
            value = stringResource(currentTheme.translationKey),
            onValueChange = {},
            readOnly = true,
            leadingIcon = {
                CapyTheme(
                    appTheme = currentTheme,
                    pureBlack = pureBlackDarkMode,
                    themeMode = themeMode,
                    preview = true,
                ) {
                    ThemePreviewSquare()
                }
            },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors(
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            ),
            modifier = Modifier
                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            appThemes.forEach { appTheme ->
                DropdownMenuItem(
                    text = {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CapyTheme(
                                appTheme = appTheme,
                                pureBlack = pureBlackDarkMode,
                                themeMode = themeMode,
                                preview = true,
                            ) {
                                ThemePreviewSquare()
                            }
                            Text(stringResource(appTheme.translationKey))
                        }
                    },
                    onClick = {
                        appPreferences.appTheme.set(appTheme)
                        onChange()
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemePreviewSquare() {
    Row(
        modifier = Modifier
            .size(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outline,
                shape = RoundedCornerShape(8.dp),
            )
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.background)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.primary)
        )
    }
}

@PreviewLightDark
@Composable
private fun ThemePickerPreview() {
    val context = LocalContext.current
    val preferences = AppPreferences(context)

    CapyTheme {
        Surface {
            ThemePicker(appPreferences = preferences)
        }
    }
}
