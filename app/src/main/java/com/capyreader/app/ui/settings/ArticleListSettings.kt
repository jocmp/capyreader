package com.capyreader.app.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.ui.components.DialogHorizontalDivider
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
