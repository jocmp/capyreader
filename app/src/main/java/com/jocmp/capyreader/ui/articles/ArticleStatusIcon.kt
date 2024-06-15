package com.jocmp.capyreader.ui.articles

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.painterResource
import com.jocmp.capy.ArticleStatus
import com.jocmp.capyreader.R

@Composable
fun ArticleStatusIcon(status: ArticleStatus) {
    return when (status) {
        ArticleStatus.ALL -> Icon(
            painter = painterResource(R.drawable.icon_notes),
            contentDescription = null
        )

        ArticleStatus.UNREAD -> Icon(
            painterResource(R.drawable.icon_circle_filled),
            contentDescription = null
        )

        ArticleStatus.STARRED -> Icon(
            painterResource(R.drawable.icon_star_filled),
            contentDescription = null
        )
    }
}
