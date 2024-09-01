package com.capyreader.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.R
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.ui.components.TextSwitch

@Immutable
data class ArticleListOptions(
    val imagePreview: ImagePreview,
    val showFeedIcons: Boolean,
    val showFeedName: Boolean,
    val showSummary: Boolean,
    val updateFeedIcons: (show: Boolean) -> Unit,
    val updateFeedName: (show: Boolean) -> Unit,
    val updateImagePreview: (preview: ImagePreview) -> Unit,
    val updateSummary: (show: Boolean) -> Unit,
)

@Composable
fun ArticleListSettings(
    options: ArticleListOptions,
) {
    Column {
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
        ImagePreviewMenu(
            onUpdateImagePreview = options.updateImagePreview,
            imagePreview = options.imagePreview
        )
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
            showFeedName = false,
            updateImagePreview = {},
            updateSummary = {},
            updateFeedName = {},
            updateFeedIcons = {},
        )
    )
}
