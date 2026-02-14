package com.capyreader.app.ui.articles.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloseFullscreen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun ArticleNavigationIcon(
    isFullscreen: Boolean = false,
    onExitFullscreen: () -> Unit = {},
    onClick: () -> Unit,
) {
    if (isFullscreen) {
        IconButton(onClick = onExitFullscreen) {
            Icon(
                imageVector = Icons.Rounded.CloseFullscreen,
                modifier = Modifier.rotate(90f),
                contentDescription = null
            )
        }
    } else {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = null
            )
        }
    }
}

@Preview
@Composable
private fun ArticleNavigationIconPreview() {
    CapyTheme {
        ArticleNavigationIcon(isFullscreen = true) { }
    }
}
