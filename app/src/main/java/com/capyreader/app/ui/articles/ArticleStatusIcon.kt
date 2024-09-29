package com.capyreader.app.ui.articles

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import com.jocmp.capy.ArticleStatus

@Composable
fun ArticleStatusIcon(status: ArticleStatus) {
    return when (status) {
        ArticleStatus.ALL -> Icon(
            Icons.AutoMirrored.Rounded.Notes,
            contentDescription = null
        )

        ArticleStatus.UNREAD -> Icon(
            Icons.Rounded.Circle,
            contentDescription = null
        )

        ArticleStatus.STARRED -> Icon(
            Icons.Rounded.Star,
            contentDescription = null
        )
    }
}
