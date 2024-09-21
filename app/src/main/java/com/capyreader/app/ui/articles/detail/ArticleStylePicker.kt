package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.components.LabelStyle
import com.jocmp.capy.articles.TextSize
import com.capyreader.app.ui.theme.CapyTheme
import org.koin.compose.koinInject
import kotlin.math.roundToInt

@Composable
fun ArticleStylePicker(
    appPreferences: AppPreferences = koinInject(),
    onChange: () -> Unit = {}
) {
    val textSizes = TextSize.sorted

    var fontFamily by remember { mutableStateOf(appPreferences.readerOptions.fontFamily.get()) }
    var sliderPosition by remember {
        mutableFloatStateOf(textSizes.indexOf(appPreferences.readerOptions.textSize.get()).toFloat())
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        ArticleFontMenu(
            updateFontFamily = { font ->
                fontFamily = font
                appPreferences.readerOptions.fontFamily.set(font)
                onChange()
            },
            fontOption = fontFamily
        )

        Column {
            FormSection(
                labelStyle = LabelStyle.COMPACT,
                title = stringResource(R.string.article_font_scale_label)
            ) {
                Slider(
                    steps = textSizes.size - 2,
                    valueRange = 0f..(textSizes.size - 1).toFloat(),
                    value = sliderPosition,
                    onValueChange = {
                        sliderPosition = it
                        appPreferences.readerOptions.textSize.set(TextSize.sorted[it.roundToInt()])
                        onChange()
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun ArticleStyleBottomSheetPreview() {
    val context = LocalContext.current

    CapyTheme {
        ArticleStylePicker(AppPreferences(context))
    }
}
