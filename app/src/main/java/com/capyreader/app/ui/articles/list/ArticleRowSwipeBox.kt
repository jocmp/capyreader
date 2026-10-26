package com.capyreader.app.ui.articles.list

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.jocmp.capy.Article
import me.saket.swipe.SwipeableActionsBox

@Composable
fun ArticleRowSwipeBox(
    article: Article,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val swipeState = rememberArticleRowSwipeState(article = article)

    if (swipeState.disabled) {
        Box(modifier) {
            content()
        }
    } else {
        SwipeableActionsBox(
            startActions = swipeState.start,
            endActions = swipeState.end,
            backgroundUntilSwipeThreshold = MaterialTheme.colorScheme.surface,
            modifier = modifier,
        ) {
            content()
        }
    }
}
