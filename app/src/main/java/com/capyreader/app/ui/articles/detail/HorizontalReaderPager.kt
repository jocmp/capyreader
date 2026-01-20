package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.paging.compose.LazyPagingItems
import com.capyreader.app.common.AudioEnclosure
import com.capyreader.app.common.Media
import com.jocmp.capy.Article

@Composable
fun HorizontalReaderPager(
    enabled: Boolean,
    articles: LazyPagingItems<Article>,
    currentArticle: Article,
    currentIndex: Int,
    onSelectArticle: (String) -> Unit,
    onSelectMedia: (media: Media) -> Unit,
    onSelectAudio: (audio: AudioEnclosure) -> Unit,
    onPauseAudio: () -> Unit,
    currentAudioUrl: String?,
    isAudioPlaying: Boolean,
) {
    // Fall back to single article view only when pager is disabled or no articles loaded
    if (!enabled || articles.itemCount == 0) {
        ArticleReader(
            article = currentArticle,
            onSelectMedia = onSelectMedia,
            onSelectAudio = onSelectAudio,
            onPauseAudio = onPauseAudio,
            currentAudioUrl = currentAudioUrl,
            isAudioPlaying = isAudioPlaying,
        )
        return
    }

    // Track initial index to avoid scrolling when article wasn't in list initially
    var initialIndex by remember(currentArticle.id) { mutableIntStateOf(currentIndex) }

    // Use page 0 when article not found in list (e.g., opened from notification)
    val effectiveIndex = maxOf(0, currentIndex)

    val pagerState = rememberPagerState(
        initialPage = effectiveIndex,
        pageCount = { articles.itemCount }
    )

    SyncPagerToSelection(
        pagerState = pagerState,
        currentIndex = currentIndex,
        initialIndex = initialIndex,
    )

    SyncSelectionToPager(
        pagerState = pagerState,
        articles = articles,
        currentIndex = currentIndex,
        onSelectArticle = onSelectArticle,
    )

    HorizontalPager(
        state = pagerState,
        modifier = Modifier.fillMaxSize(),
        beyondViewportPageCount = 0,
        key = { page ->
            // Use currentArticle.id as key when article wasn't in list initially
            // This prevents key changes from causing page recreation/flash
            if (initialIndex == -1) currentArticle.id else articles.peek(page)?.id ?: page
        },
    ) { page ->
        // Show currentArticle when:
        // - This is the selected page (for updated state like full content)
        // - Article wasn't in list initially (initialIndex == -1) - keep showing it to avoid flash
        val article = when {
            page == currentIndex -> currentArticle
            initialIndex == -1 -> currentArticle
            else -> articles[page]
        }

        if (article != null) {
            ArticleReader(
                article = article,
                onSelectMedia = onSelectMedia,
                onSelectAudio = onSelectAudio,
                onPauseAudio = onPauseAudio,
                currentAudioUrl = currentAudioUrl,
                isAudioPlaying = isAudioPlaying,
            )
        } else {
            Box(modifier = Modifier.fillMaxSize())
        }
    }
}

@Composable
private fun SyncPagerToSelection(
    pagerState: PagerState,
    currentIndex: Int,
    initialIndex: Int,
) {
    LaunchedEffect(currentIndex) {
        // Skip scroll if article wasn't in list initially (avoid flash when index becomes valid)
        if (initialIndex == -1) return@LaunchedEffect

        if (currentIndex >= 0 && pagerState.currentPage != currentIndex) {
            pagerState.scrollToPage(currentIndex)
        }
    }
}

@Composable
private fun SyncSelectionToPager(
    pagerState: PagerState,
    articles: LazyPagingItems<Article>,
    currentIndex: Int,
    onSelectArticle: (String) -> Unit,
) {
    val settledPage = pagerState.settledPage
    val articleAtSettledPage = articles.peek(settledPage)

    // Re-run when settledPage changes or when the article at that page loads
    LaunchedEffect(settledPage, articleAtSettledPage?.id) {
        // Don't sync selection if article not in list (currentIndex == -1)
        // This prevents selecting a different article when opened from notification
        if (currentIndex == -1) return@LaunchedEffect

        if (articleAtSettledPage != null) {
            onSelectArticle(articleAtSettledPage.id)
        }
    }
}
