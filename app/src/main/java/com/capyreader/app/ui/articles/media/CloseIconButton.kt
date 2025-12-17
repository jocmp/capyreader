package com.capyreader.app.ui.articles.media

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun CloseIconButton(
    onClick: () -> Unit = {}
) {
    Box(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp),
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.TopStart)
                .clip(CircleShape)
                .background(
                    color = Color.Black.copy(alpha = 0.6f),
                    shape = CircleShape
                )
                .clickable { onClick() }
                .clipToBounds()
        ) {
            Icon(
                Icons.Rounded.Close,
                contentDescription = null,
                tint = Color.White
            )
        }
    }
}

@Preview
@Composable
private fun CloseIconButtonPreview() {
    CapyTheme {
        CloseIconButton(onClick = {})
    }
}
