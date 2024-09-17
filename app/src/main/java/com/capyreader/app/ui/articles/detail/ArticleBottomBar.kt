package com.capyreader.app.ui.articles.detail

import android.util.Log
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.ThemeOption
import com.capyreader.app.ui.theme.CapyTheme

@Composable
fun ArticleBottomBar(
    onRequestNext: () -> Unit = {},
    onRequestPrevious: () -> Unit = {},
    showPrevious: Boolean = true,
    showNext: Boolean = true,
) {
    BottomBar {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { onRequestPrevious() },
                enabled = showPrevious,
            ) {
                Icon(
                    Icons.Rounded.KeyboardArrowUp,
                    contentDescription = stringResource(R.string.article_bottom_bar_next_article)
                )
            }

            Spacer(Modifier.width(16.dp))

            IconButton(
                onClick = { onRequestNext() },
                enabled = showNext,
            ) {
                Icon(
                    Icons.Rounded.KeyboardArrowDown,
                    contentDescription = stringResource(R.string.article_bottom_bar_next_article)
                )
            }
        }
    }
}

@Composable
fun BottomBar(
    content: @Composable BoxScope.() -> Unit
) {
    val isAtBottom = false // scrollState.maxValue == scrollState.value

    val color by animateColorAsState(
        label = "",
        targetValue = if (isAtBottom) {
            MaterialTheme.colorScheme.surfaceContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainerHighest
        },
    )


    Surface {
        HorizontalDivider(
            color = color
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
