package com.capyreader.app.ui.articles

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.capyreader.app.R
import com.jocmp.capy.ArticleStatus

@Composable
fun ArticleStatusIcon(status: ArticleStatus) {
    return when (status) {
        ArticleStatus.ALL -> Icon(
            Icons.AutoMirrored.Rounded.Notes,
            contentDescription = null
        )

        ArticleStatus.UNREAD -> Icon(
            painterResource(R.drawable.icon_circle_filled),
            contentDescription = null,
        )

        ArticleStatus.STARRED -> Icon(
            Icons.Rounded.Star,
            contentDescription = null
        )
    }
}
