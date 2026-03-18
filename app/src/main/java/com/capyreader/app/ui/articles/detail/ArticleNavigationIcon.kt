package com.capyreader.app.ui.articles.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloseFullscreen
import androidx.compose.material.icons.rounded.OpenInFull
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.ui.isCompact
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun ArticleNavigationIcon(
    isFullscreen: Boolean = false,
    onToggleFullscreen: () -> Unit = {},
    onClose: () -> Unit,
) {
    if (isCompact()) {
        IconButton(onClick = onClose) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = null
            )
        }
    } else if (isFullscreen) {
        IconButton(onClick = onToggleFullscreen) {
            Icon(
                imageVector = Icons.Rounded.CloseFullscreen,
                contentDescription = null
            )
        }
    } else {
        IconButton(onClick = onToggleFullscreen) {
            Icon(
                imageVector = Icons.Rounded.OpenInFull,
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
