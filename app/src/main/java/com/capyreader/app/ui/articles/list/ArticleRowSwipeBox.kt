package com.capyreader.app.ui.articles.list

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.jocmp.capy.Article

@Composable
fun ArticleRowSwipeBox(
    article: Article,
    content: @Composable () -> Unit
) {
    val swipeState = rememberArticleRowSwipeState(article = article)
    val dismissState = swipeState.state
    val action = swipeState.action

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = swipeState.enableStart,
        enableDismissFromEndToStart = swipeState.enableEnd,
        gesturesEnabled = swipeState.enabled,
        backgroundContent = {
            val isRtl = LocalLayoutDirection.current == LayoutDirection.Rtl

            val color by animateColorAsState(
                when (swipeState.state.targetValue) {
                    SwipeToDismissBoxValue.Settled -> MaterialTheme.colorScheme.surface
                    else -> MaterialTheme.colorScheme.surfaceContainerHighest
                },
                label = ""
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color)
            ) {
                Icon(
                    painterResource(action.icon),
                    contentDescription = stringResource(id = action.translationKey),
                    modifier = Modifier
                        .padding(24.dp)
                        .align(
                            when (dismissState.dismissDirection) {
                                SwipeToDismissBoxValue.StartToEnd -> if (isRtl) Alignment.CenterEnd else Alignment.CenterStart
                                else ->  if (isRtl) Alignment.CenterStart else Alignment.CenterEnd
                            }
                        )
                )
            }
        }
    ) {
        content()
    }

    if (dismissState.currentValue != SwipeToDismissBoxValue.Settled) {
        LaunchedEffect(Unit) {
            action.commit()
            dismissState.reset()
        }
    }
}
