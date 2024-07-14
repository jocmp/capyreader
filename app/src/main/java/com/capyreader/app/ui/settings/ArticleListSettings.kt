package com.capyreader.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.ImagePreview
import com.capyreader.app.ui.components.DialogCard
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
    onRequestClose: () -> Unit,
    options: ArticleListOptions,
) {
    DialogCard {
        Column {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Filled.Palette,
                    contentDescription = null,
                    tint = colorScheme.secondary,
                    modifier = Modifier.padding(16.dp)
                )
                Text(
                    text = "Display Options",
                    style = typography.headlineSmall
                )
            }
            DialogHorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Column(
                Modifier
                    .weight(0.1f, fill = false)
                    .heightIn(max = 300.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    Modifier.padding(16.dp)
                ) {
                    TextSwitch(
                        onCheckedChange = options.updateFeedName,
                        checked = options.showFeedName
                    ) {
                        Text(stringResource(R.string.settings_article_list_feed_name))
                    }
                    TextSwitch(
                        onCheckedChange = options.updateFeedIcons,
                        checked = options.showFeedIcons
                    ) {
                        Text(stringResource(R.string.settings_article_list_feed_icons))
                    }
                    TextSwitch(
                        onCheckedChange = options.updateSummary,
                        checked = options.showSummary
                    ) {
                        Text(stringResource(R.string.settings_article_list_summary))
                    }
                    ImagePreviewMenu(
                        onUpdateImagePreview = options.updateImagePreview,
                        imagePreview = options.imagePreview
                    )
                }
            }
            DialogHorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                TextButton(onClick = { onRequestClose() }) {
                    Text("Close")
                }
            }
        }
    }
}

@Preview
@Composable
private fun ArticleListSettingsPreview() {
    ArticleListSettings(
        onRequestClose = {},
        options = ArticleListOptions(
            imagePreview = ImagePreview.default,
            showSummary = true,
            showFeedIcons = true,
            showFeedName = false,
            updateImagePreview = {},
            updateSummary = { },
            updateFeedName = {},
            updateFeedIcons = {},
        )
    )
}
