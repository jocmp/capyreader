package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.capyreader.app.R
import com.capyreader.app.common.shareLink
import com.capyreader.app.ui.components.DialogCard
import com.capyreader.app.ui.components.ShareLink
import com.capyreader.app.ui.components.buildCopyToClipboard
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun ShareLinkDialog(
    onClose: () -> Unit,
    link: ShareLink,
) {
    val listItemColors =
        ListItemDefaults.colors(containerColor = CardDefaults.cardColors().containerColor)

    val context = LocalContext.current
    val shareLink = {
        context.shareLink(url = link.url, title = link.text)
        onClose()
    }

    val copy = buildCopyToClipboard(link.url)

    val copyLink = {
        copy()
        onClose()
    }

    Dialog(onDismissRequest = onClose) {
        DialogCard {
            Column(
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                ) {
                    Text(
                        link.text,
                        maxLines = 1,
                        fontSize = 14.sp,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        link.url,
                        fontSize = 14.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                HorizontalDivider()
                Column(
                    Modifier.padding(vertical = 8.dp)
                ) {
                    ListItem(
                        modifier = Modifier.clickable {
                            copyLink()
                        },
                        colors = listItemColors,
                        leadingContent = {
                            Icon(
                                Icons.Rounded.ContentCopy,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text(stringResource(R.string.actions_copy_link)) }
                    )

                    ListItem(
                        modifier = Modifier.clickable {
                            shareLink()
                        },
                        colors = listItemColors,
                        leadingContent = {
                            Icon(
                                Icons.Rounded.Share,
                                contentDescription = null
                            )
                        },
                        headlineContent = { Text(stringResource(R.string.actions_share_link)) }
                    )
                }
            }
        }
    }
}

@Preview
@Composable
fun ShareLinkDialogPreview() {
    CapyTheme {
        ShareLinkDialog(
            onClose = {},
            link = ShareLink(
                text = "My Title Text",
                url = "https://asteriastudio.com/collections/wallpaper"
            )
        )
    }
}
