package com.capyreader.app.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.AppTheme
import com.capyreader.app.ui.collectChangesWithCurrent
import com.capyreader.app.ui.theme.CapyTheme
import org.koin.compose.koinInject

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ThemeCarousel(
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
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        appThemes.forEach { appTheme ->
            ToolbarTooltip(
                message = stringResource(appTheme.translationKey)
            ) {
                CapyTheme(
                    appTheme = appTheme,
                    pureBlack = pureBlackDarkMode,
                    themeMode = themeMode,
                    preview = true,
                ) {
                    AppThemePreviewItem(
                        selected = currentTheme == appTheme,
                        onClick = {
                            appPreferences.appTheme.set(appTheme)
                            onChange()
                        },
                    )
                }
            }
        }
    }
}

@Composable
fun AppThemePreviewItem(
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .border(
                width = 3.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                shape = RoundedCornerShape(12.dp),
            )
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@PreviewLightDark
@Composable
private fun ThemeCarouselPreview() {
    val context = LocalContext.current
    val preferences = AppPreferences(context)

    CapyTheme {
        Surface {
            ThemeCarousel(appPreferences = preferences)
        }
    }
}
