package com.jocmp.basilreader.ui.articles

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage

@Composable
fun FaviconBadge(url: String?) {
    AsyncImage(
        url,
        contentDescription = "",
        modifier = Modifier
            .size(16.dp)
            .clip(RoundedCornerShape(2.dp))
            .background(colorScheme.surfaceContainer, RoundedCornerShape(2.dp))
    )
}
