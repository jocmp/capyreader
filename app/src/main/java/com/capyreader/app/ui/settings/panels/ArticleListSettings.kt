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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capyreader.app.R
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.common.RowItem
import com.capyreader.app.preferences.AfterReadAllBehavior
import com.capyreader.app.preferences.AppTheme
import com.capyreader.app.preferences.ArticleListVerticalSwipe
import com.capyreader.app.preferences.BackAction
import com.capyreader.app.preferences.BadgeStyle
import com.capyreader.app.preferences.RowSwipeOption
import com.capyreader.app.ui.articles.ArticleListFontScale
import com.capyreader.app.ui.articles.ArticleRowOptions
import com.capyreader.app.ui.articles.FaviconBadge
import com.capyreader.app.ui.articles.MarkReadPosition
import com.capyreader.app.ui.articles.StyleProviders
import com.capyreader.app.ui.articles.list.ArticleListItem
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.components.LabelStyle
import com.capyreader.app.ui.components.TextSwitch
import com.capyreader.app.ui.settings.PreferenceSelect
import com.capyreader.app.ui.settings.filters.FilterKeywords
import com.capyreader.app.ui.settings.filters.FiltersItem
import com.capyreader.app.ui.settings.filters.LocalFilterKeywords
import com.capyreader.app.ui.theme.LocalAppTheme
import java.lang.String.CASE_INSENSITIVE_ORDER
import kotlin.math.roundToInt
import org.koin.androidx.compose.koinViewModel

@Composable
fun ArticleListPreviewRow(viewModel: DisplaySettingsViewModel = koinViewModel()) {
    PreviewArticleRow(
        rowOptions = ArticleRowOptions(
            showIcon = viewModel.showFeedIcons,
            showSummary = viewModel.showSummary,
            showFeedName = viewModel.showFeedName,
            imagePreview = viewModel.imagePreview,
            fontScale = viewModel.fontScale,
            shortenTitles = viewModel.shortenTitles,
            dim = false,
        )
    )
}

@Composable
fun ArticleListFontSizeRow(viewModel: DisplaySettingsViewModel = koinViewModel()) {
    val fontScales = ArticleListFontScale.entries

    FormSection(
        title = stringResource(R.string.article_font_scale_label),
        labelStyle = LabelStyle.COMPACT,
    ) {
        RowItem {
            Slider(
                steps = fontScales.size - 2,
                valueRange = 0f..(fontScales.size - 1).toFloat(),
                value = viewModel.fontScale.ordinal.toFloat(),
                onValueChange = {
                    viewModel.updateFontScale(fontScales[it.roundToInt()])
                }
            )
        }
    }
}

@Composable
fun UnreadBadgesRow(viewModel: DisplaySettingsViewModel = koinViewModel()) {
    PreferenceSelect(
        selected = viewModel.badgeStyle,
        update = viewModel::updateBadgeStyle,
        options = BadgeStyle.entries,
        label = R.string.settings_panel_unread_counts_title,
        optionText = { stringResource(it.translationKey) },
    )
}

@Composable
fun ShowFeedNameRow(viewModel: DisplaySettingsViewModel = koinViewModel()) {
    RowItem {
        TextSwitch(
            onCheckedChange = viewModel::updateFeedName,
            checked = viewModel.showFeedName,
            title = stringResource(R.string.settings_article_list_feed_name)
        )
    }
}

@Composable
fun ShowFeedIconsRow(viewModel: DisplaySettingsViewModel = koinViewModel()) {
    RowItem {
        TextSwitch(
            onCheckedChange = viewModel::updateFeedIcons,
            checked = viewModel.showFeedIcons,
            title = stringResource(R.string.settings_article_list_feed_icons)
        )
    }
}

@Composable
fun ShowSummaryRow(viewModel: DisplaySettingsViewModel = koinViewModel()) {
    RowItem {
        TextSwitch(
            onCheckedChange = viewModel::updateSummary,
            checked = viewModel.showSummary,
            title = stringResource(R.string.settings_article_list_summary)
        )
    }
}

@Composable
fun ShortenTitlesRow(viewModel: DisplaySettingsViewModel = koinViewModel()) {
    RowItem {
        TextSwitch(
            onCheckedChange = viewModel::updateShortenTitles,
            checked = viewModel.shortenTitles,
            title = stringResource(R.string.settings_article_list_shorten_titles)
        )
    }
}

@Composable
fun ImagePreviewRow(viewModel: DisplaySettingsViewModel = koinViewModel()) {
    PreferenceSelect(
        selected = viewModel.imagePreview,
        update = viewModel::updateImagePreview,
        options = ImagePreview.sorted,
        label = R.string.image_preview_label,
        disabledOption = ImagePreview.NONE,
        optionText = {
            stringResource(id = it.translationKey)
        }
    )
}

@Composable
fun ListSwipeStartRow(viewModel: GesturesSettingsViewModel = koinViewModel()) {
    PreferenceSelect(
        selected = viewModel.rowSwipeStart,
        update = viewModel::updateRowSwipeStart,
        options = RowSwipeOption.sorted,
        label = R.string.settings_gestures_list_row_swipe_start,
        disabledOption = RowSwipeOption.DISABLED,
        optionText = { stringResource(it.translationKey) }
    )
}

@Composable
fun ListSwipeEndRow(viewModel: GesturesSettingsViewModel = koinViewModel()) {
    PreferenceSelect(
        selected = viewModel.rowSwipeEnd,
        update = viewModel::updateRowSwipeEnd,
        options = RowSwipeOption.sorted,
        label = R.string.settings_gestures_list_row_swipe_end,
        disabledOption = RowSwipeOption.DISABLED,
        optionText = { stringResource(it.translationKey) }
    )
}

@Composable
fun ListSwipeUpRow(viewModel: GesturesSettingsViewModel = koinViewModel()) {
    PreferenceSelect(
        selected = viewModel.listSwipeBottom,
        update = viewModel::updateListSwipeBottom,
        options = ArticleListVerticalSwipe.entries,
        label = R.string.settings_gestures_list_swipe_up,
        disabledOption = ArticleListVerticalSwipe.DISABLED,
        optionText = { stringResource(it.translationKey) }
    )
}

@Composable
fun BackActionRow(viewModel: GesturesSettingsViewModel = koinViewModel()) {
    PreferenceSelect(
        selected = viewModel.backAction,
        update = viewModel::updateBackAction,
        options = BackAction.entries,
        label = R.string.settings_gestures_list_back_navigation_action,
        optionText = { stringResource(it.translationKey) }
    )
}

@Composable
fun MarkReadOnScrollRow(viewModel: GeneralSettingsViewModel = koinViewModel()) {
    RowItem {
        TextSwitch(
            onCheckedChange = viewModel::updateMarkReadOnScroll,
            checked = viewModel.markReadOnScroll,
            title = stringResource(R.string.settings_mark_read_on_scroll),
        )
    }
}

@Composable
fun ConfirmMarkAllReadRow(viewModel: GeneralSettingsViewModel = koinViewModel()) {
    RowItem {
        TextSwitch(
            onCheckedChange = viewModel::updateConfirmMarkAllRead,
            checked = viewModel.confirmMarkAllRead,
            title = stringResource(R.string.settings_confirm_mark_all_read),
        )
    }
}

@Composable
fun AfterReadAllRow(viewModel: GeneralSettingsViewModel = koinViewModel()) {
    PreferenceSelect(
        selected = viewModel.afterReadAll,
        update = viewModel::updateAfterReadAll,
        options = AfterReadAllBehavior.entries,
        label = R.string.after_read_all_behavior_label,
        optionText = {
            stringResource(id = it.translationKey)
        }
    )
}

@Composable
fun MarkAllReadPositionRow(viewModel: GeneralSettingsViewModel = koinViewModel()) {
    PreferenceSelect(
        selected = viewModel.markReadButtonPosition,
        update = viewModel::updateMarkReadButtonPosition,
        options = MarkReadPosition.entries,
        label = R.string.mark_all_read_button_position,
        optionText = {
            stringResource(id = it.translationKey)
        }
    )
}

@Composable
fun FiltersRow(viewModel: GeneralSettingsViewModel = koinViewModel()) {
    val keywords by viewModel.filterKeywords.collectAsStateWithLifecycle()

    val filterKeywords = FilterKeywords(
        keywords = keywords.toList().sortedWith(compareBy(CASE_INSENSITIVE_ORDER) { it }),
        remove = viewModel::removeFilterKeyword,
        add = viewModel::addFilterKeyword,
    )

    CompositionLocalProvider(LocalFilterKeywords provides filterKeywords) {
        FiltersItem()
    }
}

@Composable
private fun PreviewArticleRow(rowOptions: ArticleRowOptions) {
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
                    maxLines = if (rowOptions.shortenTitles) 3 else Int.MAX_VALUE,
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
                    if (rowOptions.showFeedName) {
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
            supportingContent = if (rowOptions.showSummary || rowOptions.imagePreview == ImagePreview.LARGE) {
                {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(vertical = 4.dp),
                    ) {
                        if (rowOptions.showSummary) {
                            Text(
                                text = PREVIEW_SUMMARY,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                            )
                        }
                        if (rowOptions.imagePreview == ImagePreview.LARGE) {
                            PreviewImage(imagePreview = rowOptions.imagePreview)
                        }
                    }
                }
            } else {
                null
            },
            leadingContent = if (rowOptions.showIcon) {
                { FaviconBadge(url = null) }
            } else {
                null
            },
            trailingContent = if (rowOptions.imagePreview.showInline()) {
                { PreviewImage(imagePreview = rowOptions.imagePreview) }
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
private fun PreviewArticleRowPreview() {
    PreviewArticleRow(
        rowOptions = ArticleRowOptions(
            showIcon = true,
            showSummary = true,
            showFeedName = false,
            imagePreview = ImagePreview.default,
            fontScale = ArticleListFontScale.LARGE,
            shortenTitles = true,
            dim = false,
        )
    )
}
