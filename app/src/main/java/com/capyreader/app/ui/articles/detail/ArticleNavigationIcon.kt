package com.capyreader.app.ui.articles.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun ArticleNavigationIcon(onClick: () -> Unit) {
    IconButton(
        onClick = {
            onClick()
        }
    ) {
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
        ArticleNavigationIcon {  }
    }
}
