package com.capyreader.app.ui.articles.media

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Share
import androidx.compose.runtime.Composable
import com.capyreader.app.R

@Composable
fun MediaShareButton() {
    MediaActionButton(
        onClick = {
        },
        text = R.string.media_share,
        icon = Icons.Rounded.Share,
    )
}
