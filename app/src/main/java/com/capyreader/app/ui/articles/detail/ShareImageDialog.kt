package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import coil3.compose.AsyncImage
import com.capyreader.app.R
import com.capyreader.app.ui.components.DialogCard
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun ShareImageDialog(
    onClose: () -> Unit,
    imageUrl: String,
    onSave: () -> Unit,
    onShare: () -> Unit,
) {
    val listItemColors =
        ListItemDefaults.colors(containerColor = CardDefaults.cardColors().containerColor)

    Dialog(onDismissRequest = onClose) {
        DialogCard {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(colorScheme.surfaceContainer)
                    )
                    Text(
                        extractFilename(imageUrl),
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
                HorizontalDivider()
                Column(
                    Modifier.padding(vertical = 8.dp)
                ) {
                    ListItem(
                        modifier = Modifier.clickable { onSave() },
                        colors = listItemColors,
                        leadingContent = {
                            Icon(
                                Icons.Rounded.Save,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text(stringResource(R.string.media_save)) }
                    )

                    ListItem(
                        modifier = Modifier.clickable { onShare() },
                        colors = listItemColors,
                        leadingContent = {
                            Icon(
                                Icons.Rounded.Share,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text(stringResource(R.string.media_share)) }
                    )
                }
            }
        }
    }
}

private fun extractFilename(url: String): String {
    return try {
        url.toUri().lastPathSegment ?: url
    } catch (_: Exception) {
        url
    }
}

@Preview
@Composable
private fun ShareImageDialogPreview() {
    CapyTheme {
        ShareImageDialog(
            onClose = {},
            imageUrl = "https://asteriastudio.com/images/photo.jpg",
            onSave = {},
            onShare = {},
        )
    }
}
