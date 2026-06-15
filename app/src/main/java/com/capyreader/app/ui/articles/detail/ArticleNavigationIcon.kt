package com.capyreader.app.ui.articles.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.ui.theme.CapyTheme

/**
 * The reader's leading action is always close ("X"): pane resizing is handled natively by the
 * list-detail Scene's drag handle, so the old fullscreen-toggle arrow is gone.
 */
@Composable
fun ArticleNavigationIcon(
    onClose: () -> Unit,
) {
    IconButton(onClick = onClose) {
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = null
        )
    }
}

@Preview
@Composable
private fun ArticleNavigationIconPreview() {
    CapyTheme {
        ArticleNavigationIcon { }
    }
}
