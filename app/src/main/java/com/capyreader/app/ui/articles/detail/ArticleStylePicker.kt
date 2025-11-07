package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.collectChangesWithCurrent
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.components.LabelStyle
import com.capyreader.app.ui.components.ThemeCarousel
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.articles.FontSize
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@Composable
fun ArticleStylePicker(
    appPreferences: AppPreferences = koinInject(),
) {
    val textSizes = FontSize.scale

    var fontFamily by remember { mutableStateOf(appPreferences.readerOptions.fontFamily.get()) }
    val fontSize by appPreferences.readerOptions.fontSize.collectChangesWithCurrent()
    var sliderPosition by remember(fontSize) {
        mutableFloatStateOf(
            textSizes.indexOf(fontSize).toFloat()
        )
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ThemeCarousel(
            appPreferences = appPreferences,
        )

        ArticleFontMenu(
            updateFontFamily = { font ->
                fontFamily = font
                appPreferences.readerOptions.fontFamily.set(font)
            },
            fontOption = fontFamily
        )

        Column {
            FormSection(
                labelStyle = LabelStyle.COMPACT,
                title = stringResource(R.string.article_font_scale_label)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Slider(
                        steps = textSizes.size - 2,
                        modifier = Modifier.weight(0.1f),
                        valueRange = 0f..(textSizes.size - 1).toFloat(),
                        value = sliderPosition,
                        onValueChange = {
                            sliderPosition = it
                            FontSize.scale.getOrNull(it.roundToInt())?.let { size ->
                                appPreferences.readerOptions.fontSize.set(size)
                            }
                        }
                    )
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .padding(vertical = 4.dp)
                            .widthIn(36.dp)
                    ) {
                        Text("$fontSize", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Preview
@Composable
private fun ArticleStyleBottomSheetPreview() {
    val context = LocalContext.current
    val preferences = AppPreferences(context).apply {
        readerOptions.fontSize.set(16)
    }

    CapyTheme {
        ArticleStylePicker(preferences)
    }
}
