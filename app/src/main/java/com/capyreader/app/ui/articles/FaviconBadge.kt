package com.capyreader.app.ui.articles

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.capyreader.app.R
import com.capyreader.app.preferences.ThemeMode
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.logging.CapyLog

@Composable
fun FaviconBadge(
    url: String?,
    size: Dp = 16.dp
) {
    val placeholder = painterResource(id = R.drawable.favicon_badge_placeholder)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(2.dp))
            .background(Color.White, RoundedCornerShape(2.dp))
    ) {
        Box(Modifier.padding(1.dp)) {
            if (!url.isNullOrBlank()) {
                AsyncImage(
                    url,
                    contentDescription = "",
                    modifier = Modifier.size(size),
                    error = placeholder,
                    onError = {
                        CapyLog.error("favicon_badge", it.result.throwable)
                    }
                )
            } else {
                Image(
                    placeholder,
                    contentDescription = null,
                    modifier = Modifier.size(size),
                )
            }
        }
    }
}

@Preview
@Composable
private fun FaviconBadgePreview() {
    CapyTheme {
        FaviconBadge(url = null)
    }
}


@Preview
@Composable
private fun FaviconBadgePreviewDark() {
    CapyTheme(themeMode = ThemeMode.DARK) {
        FaviconBadge(url = null)
    }
}
