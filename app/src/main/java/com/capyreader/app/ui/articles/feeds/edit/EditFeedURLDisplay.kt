package com.capyreader.app.ui.articles.feeds.edit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.R
import com.capyreader.app.ui.components.buildCopyToClipboard
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun EditFeedURLDisplay(
    url: String
) {
    val copyToClipboard = buildCopyToClipboard(url)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shapes.extraSmall)
            .clickable {
                copyToClipboard()
            }
    ) {
        ListItem(
            colors = ListItemDefaults.colors(containerColor = CardDefaults.cardColors().containerColor),
            headlineContent = {
                Text(stringResource(R.string.feed_form_feed_url_title))
            },
            supportingContent = {
                Text(
                    url,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            },
            trailingContent = {
                Icon(
                    imageVector = Icons.Filled.ContentCopy,
                    tint = colorScheme.secondary,
                    contentDescription = stringResource(R.string.feed_form_copy_feed_url_to_clipboard),
                )
            }
        )
    }
}

@Preview(widthDp = 300)
@Composable
private fun EditFeedURLDisplayShort() {
    CapyTheme {
        EditFeedURLDisplay(url = "https://www.404media.co/rss")
    }
}

@Preview(widthDp = 300)
@Composable
private fun EditFeedURLDisplayLong() {
    CapyTheme {
        EditFeedURLDisplay(url = "https://deprogrammaticaipsum.com/index.xml")
    }
}
