package com.capyreader.app.ui.articles

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.capyreader.app.preferences.BadgeStyle
import com.capyreader.app.ui.LocalBadgeStyle

@Composable
fun CountBadge(
    count: Long,
    showBadge: Boolean = true,
) {
    if (count < 1) {
        return
    }

    when (LocalBadgeStyle.current) {
        BadgeStyle.EXACT -> Text(count.toString())
        BadgeStyle.SIMPLE -> {
            if (!showBadge) {
                return
            }

            val color = LocalContentColor.current
            Canvas(modifier = Modifier.size(8.dp)) {
                drawCircle(color = color)
            }
        }
    }
}
