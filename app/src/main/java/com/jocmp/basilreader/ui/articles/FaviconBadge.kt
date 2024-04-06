package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.jocmp.basilreader.R

@Composable
fun FaviconBadge(
    url: String?,
) {
    val placeholder = painterResource(id = R.drawable.ic_rss_feed)

    if (url != null) {
        AsyncImage(
            url,
            contentDescription = "",
            modifier = Modifier
                .size(16.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colorScheme.surfaceContainer, RoundedCornerShape(2.dp))
        )
    } else {
        Image(
            placeholder,
            contentDescription = null,
            colorFilter = ColorFilter.tint(LocalContentColor.current),
            modifier = Modifier.size(16.dp)
        )
    }
}
