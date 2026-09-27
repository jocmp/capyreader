package com.capyreader.app.ui.articles

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.FiberManualRecord
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.jocmp.capy.ArticleStatus

@Composable
fun ArticleStatusIcon(
    status: ArticleStatus,
    modifier: Modifier = Modifier,
) {
    Icon(
        imageVector = status.icon,
        contentDescription = null,
        modifier = modifier,
    )
}

val ArticleStatus.icon: ImageVector
    get() = when (this) {
        ArticleStatus.ALL -> Icons.AutoMirrored.Rounded.Notes
        ArticleStatus.UNREAD -> Icons.Rounded.FiberManualRecord
        ArticleStatus.STARRED -> Icons.Rounded.Star
    }
