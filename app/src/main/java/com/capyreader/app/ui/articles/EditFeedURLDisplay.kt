package com.capyreader.app.ui.articles

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemColors
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.shapes
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.components.FormSection
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun EditFeedURLDisplay(feedURL: String) {
    val clipboardManager = LocalClipboardManager.current

    val copyToClipboard = {
        clipboardManager.setText(AnnotatedString(feedURL))
    }

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
                    feedURL,
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
        EditFeedURLDisplay(feedURL = "https://www.404media.co/rss")
    }
}

@Preview(widthDp = 300)
@Composable
private fun EditFeedURLDisplayLong() {
    CapyTheme {
        EditFeedURLDisplay(feedURL = "https://deprogrammaticaipsum.com/index.xml")
    }
}
