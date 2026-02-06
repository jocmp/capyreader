package com.capyreader.app.ui.articles.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.capyreader.app.common.AudioEnclosure
import com.capyreader.app.common.Media
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.ArticleVerticalSwipe
import com.capyreader.app.preferences.ArticleVerticalSwipe.DISABLED
import com.capyreader.app.preferences.ArticleVerticalSwipe.NEXT_ARTICLE
import com.capyreader.app.preferences.ArticleVerticalSwipe.PREVIOUS_ARTICLE
import com.capyreader.app.ui.articles.LocalFullContent
import com.capyreader.app.ui.collectChangesWithDefault
import com.capyreader.app.ui.components.pulltoload.PullToLoadIndicator
import com.capyreader.app.ui.components.pulltoload.pullToLoad
import com.capyreader.app.ui.components.pulltoload.rememberPullToLoadState
import com.capyreader.app.ui.settings.LocalSnackbarHost
import com.jocmp.capy.Article
import org.koin.compose.koinInject
import kotlin.math.abs

@Composable
fun ArticleView(
    article: Article,
    articles: LazyPagingItems<Article>,
    onBackPressed: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    enableBackHandler: Boolean = false,
    onScrollToArticle: (index: Int) -> Unit,
    onSelectArticle: (id: String) -> Unit,
    onSelectMedia: (media: Media) -> Unit,
    onSelectAudio: (audio: AudioEnclosure) -> Unit = {},
    onPauseAudio: () -> Unit = {},
    currentAudioUrl: String? = null,
    isAudioPlaying: Boolean = false,
    appPreferences: AppPreferences = koinInject()
) {
    val enableHorizontalPager by appPreferences.readerOptions.enableHorizontaPagination.collectChangesWithDefault()
    val fullContent = LocalFullContent.current

    val onToggleFullContent = {
        if (article.fullContent == Article.FullContentState.LOADED) {
            fullContent.reset()
        } else if (article.fullContent != Article.FullContentState.LOADING) {
            fullContent.fetch()
        }
    }

    val index = remember(
        article.id,
        articles.itemCount,
    ) {
        articles.itemSnapshotList.indexOfFirst { it?.id == article.id }
    }

    val previousIndex = index - 1
    val nextIndex = index + 1

    val hasPrevious = previousIndex > -1 && articles[index - 1] != null
    val hasNext = nextIndex < articles.itemCount && articles[index + 1] != null

    val previousArticleId = if (hasPrevious) articles[previousIndex]?.id else null
    val nextArticleId = if (hasNext) articles[nextIndex]?.id else null

    fun selectPrevious() {
        articles[previousIndex]?.let {
            onSelectArticle(it.id)
        }
    }

    fun selectNext() {
        articles[nextIndex]?.let {
            onSelectArticle(it.id)
        }
    }

    val onSwipe = { swipe: ArticleVerticalSwipe ->
        when (swipe) {
            PREVIOUS_ARTICLE -> selectPrevious()
            NEXT_ARTICLE -> selectNext()
            DISABLED -> {}
        }
    }

    val autoHideToolbar by appPreferences.readerOptions.autoHideToolbar.collectChangesWithDefault()
    val pullToSwitchArticle by appPreferences.readerOptions.pullToSwitchArticle.collectChangesWithDefault()
    val topSwipe by appPreferences.readerOptions.topSwipeGesture.collectChangesWithDefault()
    val bottomSwipe by appPreferences.readerOptions.bottomSwipeGesture.collectChangesWithDefault()

    val enableTopSwipe = pullToSwitchArticle && topSwipe.enabled &&
            (topSwipe != PREVIOUS_ARTICLE || hasPrevious)

    val enableBottomSwipe = pullToSwitchArticle && bottomSwipe.enabled &&
            (bottomSwipe != NEXT_ARTICLE || hasNext)

    var isReaderScrollingDown by remember { mutableStateOf(false) }
    val showToolBar = if (autoHideToolbar) !isReaderScrollingDown else true

    LaunchedEffect(article.id) {
        isReaderScrollingDown = false
    }

    val snackbarHostState = remember { SnackbarHostState() }

    CompositionLocalProvider(
        LocalSnackbarHost provides snackbarHostState,
    ) {
        Box(Modifier.fillMaxSize()) {
            // Layer 1: Content with AnimatedContent transition
            HorizontalReaderPager(
                enabled = enableHorizontalPager,
                enablePrevious = hasPrevious,
                enableNext = hasNext,
                onSelectPrevious = { selectPrevious() },
                onSelectNext = { selectNext() },
            ) {
                ArticleTransition(
                    article = article,
                    previousArticleId = previousArticleId,
                    nextArticleId = nextArticleId,
                ) { targetArticle ->
                    // resets to zero offset when the article changes.
                    val pullToLoadState = rememberPullToLoadState(
                        key = targetArticle.id,
                        onLoadPrevious = if (enableTopSwipe) {
                            { onSwipe(topSwipe) }
                        } else null,
                        onLoadNext = if (enableBottomSwipe) {
                            { onSwipe(bottomSwipe) }
                        } else null,
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pullToLoad(
                                state = pullToLoadState,
                                onScroll = { f ->
                                    if (abs(f) > 2f) isReaderScrollingDown = f < 0f
                                },
                            ),
                        contentAlignment = Alignment.Center,
                    ) {
                        ArticleReader(
                            article = targetArticle,
                            onSelectMedia = onSelectMedia,
                            onSelectAudio = onSelectAudio,
                            onPauseAudio = onPauseAudio,
                            currentAudioUrl = currentAudioUrl,
                            isAudioPlaying = isAudioPlaying,
                        )
                        PullToLoadIndicator(
                            state = pullToLoadState,
                            canLoadPrevious = enableTopSwipe,
                            canLoadNext = enableBottomSwipe,
                            topSwipeIcon = Icons.Rounded.KeyboardArrowUp,
                            bottomSwipeIcon = Icons.Rounded.KeyboardArrowDown,
                        )
                    }
                }
            }

            ArticleTopBar(
                show = showToolBar,
                articleId = article.id,
                onClose = onBackPressed,
            )

            ArticleBottomBar(
                show = showToolBar,
                article = article,
                hasNextArticle = hasNext,
                onToggleExtractContent = onToggleFullContent,
                onToggleRead = onToggleRead,
                onToggleStar = onToggleStar,
                onSelectNext = { selectNext() },
            )

            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 68.dp)
            )
        }
    }

    LaunchedEffect(index) {
        if (index > -1) {
            onScrollToArticle(index)
        }
    }

    BackHandler(enableBackHandler) {
        onBackPressed()
    }
}
