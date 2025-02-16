package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.preferences.ThemeOption
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun ArticleBottomBar(
    onRequestNext: () -> Unit = {},
    showNext: Boolean = true,
) {
    val haptics = LocalHapticFeedback.current

    val onClickFeedback = {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    BottomBar {
        IconButton(
            onClick = {
                onClickFeedback()
                onRequestNext()
            },
            enabled = showNext,
        ) {
            Icon(
                Icons.Rounded.KeyboardArrowDown,
                contentDescription = stringResource(R.string.article_bottom_bar_next_article)
            )
        }
    }
}

@Composable
fun BottomBar(
    content: @Composable BoxScope.() -> Unit
) {
    Surface {
        HorizontalDivider(
            color = MaterialTheme.colorScheme.surfaceContainerHighest
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(BottomBarTokens.ContainerHeight),
            contentAlignment = Alignment.Center,
            content = content
        )
    }

}

object BottomBarTokens {
    val ContainerHeight = 48.dp
}

@Preview
@Composable
private fun ArticleBottomBarPreview() {
    CapyTheme(
        theme = ThemeOption.DARK
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            ArticleBottomBar(

            )
        }
    }
}
