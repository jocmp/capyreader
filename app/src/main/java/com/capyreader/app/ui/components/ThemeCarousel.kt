package com.capyreader.app.ui.components

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.glance.LocalContext
import com.capyreader.app.R
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.AppTheme
import com.capyreader.app.ui.collectChangesWithCurrent
import com.capyreader.app.ui.theme.CapyTheme
import org.koin.compose.koinInject

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
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(
            items = appThemes,
            key = { it.name },
        ) { appTheme ->
            Column(
                modifier = Modifier
                    .width(114.dp)
                    .padding(top = 8.dp),
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

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(appTheme.translationKey),
                    modifier = Modifier
                        .fillMaxWidth()
                        .secondaryItemAlpha(),
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    minLines = 2,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@Composable
fun AppThemePreviewItem(
    selected: Boolean,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(9f / 16f)
            .border(
                width = 4.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    DividerDefaults.color
                },
                shape = RoundedCornerShape(17.dp),
            )
            .padding(4.dp)
            .clip(RoundedCornerShape(13.dp))
            .background(MaterialTheme.colorScheme.background)
            .clickable(onClick = onClick),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight(0.8f)
                    .weight(0.7f)
                    .padding(end = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurface,
                        shape = MaterialTheme.shapes.small,
                    ),
            )

            Box(
                modifier = Modifier.weight(0.3f),
                contentAlignment = Alignment.CenterEnd,
            ) {
                if (selected) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = stringResource(R.string.selected),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        Row(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .height(16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(5.dp)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary),
            )
        }
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .background(
                    color = DividerDefaults.color,
                    shape = MaterialTheme.shapes.small,
                )
                .aspectRatio(16 / 9f),
        )
        Box(
            modifier = Modifier
                .padding(top = 6.dp)
                .padding(horizontal = 8.dp)
                .height(6.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.onSurface),
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 2.dp)
                .height(6.dp)
                .fillMaxWidth(0.75f)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.onSurface),
        )
        Box(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .height(6.dp)
                .fillMaxWidth(0.9f)
                .clip(RoundedCornerShape(5.dp))
                .background(MaterialTheme.colorScheme.onSurface),
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.BottomCenter,
        ) {
            Surface(
                color = MaterialTheme.colorScheme.surfaceContainerHigh,
            ) {
                Row(
                    modifier = Modifier
                        .height(32.dp)
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(17.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary,
                                shape = CircleShape,
                            ),
                    )
                    Box(
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .alpha(0.6f)
                            .height(17.dp)
                            .weight(1f)
                            .background(
                                color = MaterialTheme.colorScheme.onSurface,
                                shape = MaterialTheme.shapes.small,
                            ),
                    )
                }
            }
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

fun Modifier.secondaryItemAlpha(): Modifier = this.alpha(SECONDARY_ALPHA)

const val SECONDARY_ALPHA = 0.78f
