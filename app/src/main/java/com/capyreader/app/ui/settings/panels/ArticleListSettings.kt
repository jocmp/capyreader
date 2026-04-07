package com.capyreader.app.ui.settings.panels

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.common.RowItem
import com.capyreader.app.preferences.AppTheme
import com.capyreader.app.ui.articles.ArticleListFontScale
import com.capyreader.app.ui.articles.ArticleRowOptions
import com.capyreader.app.ui.articles.FaviconBadge
import com.capyreader.app.ui.articles.StyleProviders
import com.capyreader.app.ui.articles.list.ArticleListItem
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.components.LabelStyle
import com.capyreader.app.ui.components.TextSwitch
import com.capyreader.app.ui.settings.PreferenceSelect
import com.capyreader.app.ui.theme.LocalAppTheme
import kotlin.math.roundToInt

@Immutable
data class ArticleListOptions(
    val imagePreview: ImagePreview,
    val showFeedIcons: Boolean,
    val showFeedName: Boolean,
    val showSummary: Boolean,
    val shortenTitles: Boolean,
    val fontScale: ArticleListFontScale,
    val updateFeedIcons: (show: Boolean) -> Unit,
    val updateFeedName: (show: Boolean) -> Unit,
    val updateImagePreview: (preview: ImagePreview) -> Unit,
    val updateSummary: (show: Boolean) -> Unit,
    val updateFontScale: (scale: ArticleListFontScale) -> Unit,
    val updateShortenTitles: (show: Boolean) -> Unit,
)

@Composable
fun ArticleListSettings(
    options: ArticleListOptions,
) {
    val fontScales = ArticleListFontScale.entries

    Column {
        PreviewArticleRow(options = options)

        FormSection(
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
            TextSwitch(
                onCheckedChange = options.updateShortenTitles,
                checked = options.shortenTitles,
                title = stringResource(R.string.settings_article_list_shorten_titles)
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
    }
}

@Composable
private fun PreviewArticleRow(options: ArticleListOptions) {
    val rowOptions = ArticleRowOptions(
        showIcon = options.showFeedIcons,
        showSummary = options.showSummary,
        showFeedName = options.showFeedName,
        imagePreview = options.imagePreview,
        fontScale = options.fontScale,
        shortenTitles = options.shortenTitles,
        dim = false,
    )
    val colors = ListItemDefaults.colors()
    val overlineColor = colors.overlineContentColor

    StyleProviders(options = rowOptions) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant,
                    shape = MaterialTheme.shapes.medium,
                )
        ) {
        ArticleListItem(
            headlineContent = {
                Text(
                    text = PREVIEW_TITLE,
                    maxLines = if (options.shortenTitles) 3 else Int.MAX_VALUE,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.Bold,
                )
            },
            overlineContent = {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 2.dp)
                ) {
                    if (options.showFeedName) {
                        Text(
                            text = PREVIEW_FEED_NAME,
                            color = overlineColor,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(Modifier.width(16.dp))
                    }
                    Text(
                        text = PREVIEW_TIME,
                        color = overlineColor,
                        maxLines = 1,
                    )
                }
            },
            supportingContent = if (options.showSummary || options.imagePreview == ImagePreview.LARGE) {
                {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(vertical = 4.dp),
                    ) {
                        if (options.showSummary) {
                            Text(
                                text = PREVIEW_SUMMARY,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (options.imagePreview == ImagePreview.LARGE) {
                            PreviewImage(imagePreview = options.imagePreview)
                        }
                    }
                }
            } else {
                null
            },
            leadingContent = if (options.showFeedIcons) {
                { FaviconBadge(url = null) }
            } else {
                null
            },
            trailingContent = if (options.imagePreview.showInline()) {
                { PreviewImage(imagePreview = options.imagePreview) }
            } else {
                null
            },
        )
        }
    }
}

@Composable
private fun PreviewImage(imagePreview: ImagePreview) {
    val sizeModifier = when (imagePreview) {
        ImagePreview.SMALL -> Modifier.size(56.dp)
        ImagePreview.MEDIUM -> Modifier.size(84.dp)
        else -> Modifier.fillMaxWidth().aspectRatio(3 / 2f)
    }

    val shape = MaterialTheme.shapes.small

    Box(
        contentAlignment = Alignment.Center,
        modifier = sizeModifier
            .monochromeBorder(shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Icon(
            painter = painterResource(R.drawable.icon_empty_list),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
            modifier = Modifier.size(
                when (imagePreview) {
                    ImagePreview.SMALL -> 48.dp
                    ImagePreview.MEDIUM -> 64.dp
                    else -> 80.dp
                }
            )
        )
    }
}

@Composable
private fun Modifier.monochromeBorder(shape: Shape): Modifier {
    val isMonochrome = LocalAppTheme.current.value == AppTheme.MONOCHROME

    return if (isMonochrome) {
        border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline,
            shape = shape,
        )
    } else {
        this
    }
}

private const val PREVIEW_TITLE = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua"
private const val PREVIEW_FEED_NAME = "Lorem Ipsum"
private const val PREVIEW_TIME = "3h"
private const val PREVIEW_SUMMARY = "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam."

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
            shortenTitles = true,
            updateImagePreview = {},
            updateSummary = {},
            updateFeedName = {},
            updateFeedIcons = {},
            updateFontScale = {},
            updateShortenTitles = {},
        )
    )
}
