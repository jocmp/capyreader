package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.common.RowItem
import com.capyreader.app.ui.articles.ArticleListFontScale
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.components.LabelStyle
import com.capyreader.app.ui.components.TextSwitch
import com.capyreader.app.ui.settings.PreferenceSelect
import kotlin.math.roundToInt

@Immutable
data class ArticleListOptions(
    val imagePreview: ImagePreview,
    val showFeedIcons: Boolean,
    val showFeedName: Boolean,
    val showSummary: Boolean,
    val fontScale: ArticleListFontScale,
    val updateFeedIcons: (show: Boolean) -> Unit,
    val updateFeedName: (show: Boolean) -> Unit,
    val updateImagePreview: (preview: ImagePreview) -> Unit,
    val updateSummary: (show: Boolean) -> Unit,
    val updateFontScale: (scale: ArticleListFontScale) -> Unit,
)

@Composable
fun ArticleListSettings(
    options: ArticleListOptions,
) {
    val fontScales = ArticleListFontScale.entries

    Column {
        RowItem {
            TextSwitch(
                onCheckedChange = options.updateFeedName,
                checked = options.showFeedName,
                title = stringResource(R.string.settings_article_list_feed_name)
            )
            TextSwitch(
                onCheckedChange = options.updateFeedIcons,
                checked = options.showFeedIcons,
                title = stringResource(R.string.settings_article_list_feed_icons)
            )
            TextSwitch(
                onCheckedChange = options.updateSummary,
                checked = options.showSummary,
                title = stringResource(R.string.settings_article_list_summary)
            )
        }

        PreferenceSelect(
            selected = options.imagePreview,
            update = options.updateImagePreview,
            options = ImagePreview.sorted,
            label = R.string.image_preview_label,
            disabledOption = ImagePreview.NONE,
            optionText = {
                stringResource(id = it.translationKey)
            }
        )

        FormSection(
            modifier = Modifier.padding(top = 16.dp),
            title = stringResource(R.string.article_font_scale_label),
            labelStyle = LabelStyle.COMPACT,
        ) {
            RowItem {
                Slider(
                    steps = fontScales.size - 2,
                    valueRange = 0f..(fontScales.size - 1).toFloat(),
                    value = options.fontScale.ordinal.toFloat(),
                    onValueChange = {
                        options.updateFontScale(fontScales[it.roundToInt()])
                    }
                )
            }
        }
    }
}

@Preview
@Composable
private fun ArticleListSettingsPreview() {
    ArticleListSettings(
        options = ArticleListOptions(
            imagePreview = ImagePreview.default,
            showSummary = true,
            showFeedIcons = true,
            fontScale = ArticleListFontScale.LARGE,
            showFeedName = false,
            updateImagePreview = {},
            updateSummary = {},
            updateFeedName = {},
            updateFeedIcons = {},
            updateFontScale = {}
        )
    )
}
