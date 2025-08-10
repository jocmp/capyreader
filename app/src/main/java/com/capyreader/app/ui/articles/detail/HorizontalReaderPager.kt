package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.jocmp.capy.Article
import com.jocmp.capy.logging.CapyLog
import me.saket.swipe.SwipeAction

@Composable
fun HorizontalReaderPager(
    articles: LazyPagingItems<Article>,
    initialIndex: Int,
    onSelectArticle: (index: Int, id: String) -> Unit,
    enablePrevious: Boolean,
    enableNext: Boolean,
    onSelectPrevious: () -> Unit,
    onSelectNext: () -> Unit,
    content: @Composable (article: Article) -> Unit,
) {
    if (articles.itemCount < 1 || initialIndex < 0) {
        return
    }

    val pager = rememberPagerState(
        initialPage = initialIndex
    ) {
       articles.itemCount
    }

    HorizontalPager(pager) { page ->
        val article = articles[page]

        if (article != null) {
            content(article)
        }
    }

    LaunchedEffect(pager.currentPage) {
        val index = pager.currentPage

        CapyLog.info("index", mapOf("value" to index.toString()))
        val article = articles[index]

        if (article != null) {
            onSelectArticle(index, article.id)
        }
    }

//    SwipeableActionsBox(
//        disableRipple = true,
//        swipeThreshold = 24.dp,
//        backgroundUntilSwipeThreshold = colorScheme.surface,
//        startActions = action(
//            enabled = enablePrevious,
//            icon = Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
//            onSwipe = onSelectPrevious,
//        ),
//        endActions = action(
//            enabled = enableNext,
//            icon = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
//            onSwipe = onSelectNext
//        )
//    ) {
//        content()
//    }
}

@Composable
fun action(
    enabled: Boolean,
    icon: ImageVector,
    onSwipe: () -> Unit
): List<SwipeAction> {
    if (!enabled) {
        return emptyList()
    }

    return listOf(
        SwipeAction(
            onSwipe = onSwipe,
            background = colorScheme.surfaceContainerHighest,
            icon = {
               Box(Modifier.padding(16.dp)) {
                   Icon(
                       icon,
                       contentDescription = null,
                   )
               }
            }
        )
    )
}
