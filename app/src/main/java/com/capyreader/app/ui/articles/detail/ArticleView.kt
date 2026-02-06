package com.capyreader.app.ui.articles.detail

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.paging.compose.LazyPagingItems
import com.capyreader.app.common.AudioEnclosure
import com.capyreader.app.common.Media
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.ArticleVerticalSwipe
import com.capyreader.app.preferences.ArticleVerticalSwipe.DISABLED
import com.capyreader.app.preferences.ArticleVerticalSwipe.LOAD_FULL_CONTENT
import com.capyreader.app.preferences.ArticleVerticalSwipe.NEXT_ARTICLE
import com.capyreader.app.preferences.ArticleVerticalSwipe.OPEN_ARTICLE_IN_BROWSER
import com.capyreader.app.preferences.ArticleVerticalSwipe.PREVIOUS_ARTICLE
import com.capyreader.app.ui.LocalLinkOpener
import com.capyreader.app.ui.articles.LocalFullContent
import com.capyreader.app.ui.collectChangesWithDefault
import com.capyreader.app.ui.components.pullrefresh.SwipeRefresh
import com.capyreader.app.ui.settings.LocalSnackbarHost
import com.jocmp.capy.Article
import org.koin.compose.koinInject

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
    val openLink = articleOpenLink(article)

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
        if (previousIndex < 0) return

        articles[previousIndex]?.let {
            onSelectArticle(it.id)
        }
    }

    fun selectNext() {
        if (nextIndex >= articles.itemCount) return

        articles[nextIndex]?.let {
            onSelectArticle(it.id)
        }
    }

    val onSwipe = { swipe: ArticleVerticalSwipe ->
        when (swipe) {
            LOAD_FULL_CONTENT -> onToggleFullContent()
            OPEN_ARTICLE_IN_BROWSER -> openLink()
            PREVIOUS_ARTICLE -> selectPrevious()
            NEXT_ARTICLE -> selectNext()
            DISABLED -> {}
        }
    }

    val pinToolbar by appPreferences.readerOptions.pinTopToolbar.collectChangesWithDefault()
    var isReaderScrollingDown by remember { mutableStateOf(false) }
    val showToolBar = pinToolbar || !isReaderScrollingDown

    val scrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (kotlin.math.abs(available.y) > 2f) {
                    isReaderScrollingDown = available.y < 0f
                }
                return Offset.Zero
            }
        }
    }

    LaunchedEffect(article.id) {
        isReaderScrollingDown = false
    }

    val snackbarHostState = remember { SnackbarHostState() }

    CompositionLocalProvider(
        LocalSnackbarHost provides snackbarHostState,
    ) {
        Box(Modifier.fillMaxSize().nestedScroll(scrollConnection)) {
            ArticlePullRefresh(
                onSwipe = onSwipe,
                hasPreviousArticle = hasPrevious,
                hasNextArticle = hasNext,
            ) {
                HorizontalReaderPager(
                    enabled = enableHorizontalPager,
                    enablePrevious = hasPrevious,
                    enableNext = hasNext,
                    onSelectPrevious = { selectPrevious() },
                    onSelectNext = { selectNext() },
                ) {
                    ArticleTransition(
                        article = article,
                        enableHorizontalPager = enableHorizontalPager,
                        previousArticleId = previousArticleId,
                        nextArticleId = nextArticleId,
                    ) { targetArticle ->
                        ArticleReader(
                            article = targetArticle,
                            onSelectMedia = onSelectMedia,
                            onSelectAudio = onSelectAudio,
                            onPauseAudio = onPauseAudio,
                            currentAudioUrl = currentAudioUrl,
                            isAudioPlaying = isAudioPlaying,
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

@Composable
fun ArticlePullRefresh(
    hasNextArticle: Boolean,
    hasPreviousArticle: Boolean,
    onSwipe: (swipe: ArticleVerticalSwipe) -> Unit,
    content: @Composable () -> Unit,
) {
    val (topSwipe, bottomSwipe) = rememberSwipePreferences()
    val haptics = LocalHapticFeedback.current

    val triggerThreshold = {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    val enableTopSwipe = topSwipe.enabled &&
            (topSwipe != PREVIOUS_ARTICLE || (topSwipe.openArticle && hasPreviousArticle))

    val enableBottomSwipe = bottomSwipe.enabled &&
            (bottomSwipe != NEXT_ARTICLE || (bottomSwipe.openArticle && hasNextArticle))

    SwipeRefresh(
        onRefresh = { onSwipe(topSwipe) },
        swipeEnabled = enableTopSwipe,
        indicatorPadding = PaddingValues(top = 100.dp),
        icon = swipeIcon(
            topSwipe,
            relatedArticleIcon = Icons.Rounded.KeyboardArrowUp
        ),
        onTriggerThreshold = { triggerThreshold() }
    ) {
        SwipeRefresh(
            onRefresh = { onSwipe(bottomSwipe) },
            swipeEnabled = enableBottomSwipe,
            icon = swipeIcon(
                bottomSwipe,
                relatedArticleIcon = Icons.Rounded.KeyboardArrowDown
            ),
            onTriggerThreshold = { triggerThreshold() },
            indicatorAlignment = Alignment.BottomCenter,
        ) {
            content()
        }
    }
}

fun swipeIcon(
    swipe: ArticleVerticalSwipe,
    relatedArticleIcon: ImageVector
): ImageVector {
    return when (swipe) {
        LOAD_FULL_CONTENT -> Icons.AutoMirrored.Rounded.Article
        OPEN_ARTICLE_IN_BROWSER -> Icons.AutoMirrored.Rounded.OpenInNew
        else -> relatedArticleIcon
    }
}

@Composable
fun articleOpenLink(
    article: Article,
): () -> Unit {
    val linkOpener = LocalLinkOpener.current

    fun open() {
        val link = article.url?.toString() ?: return

        linkOpener.open(link.toUri())
    }

    return ::open
}

@Composable
private fun rememberSwipePreferences(appPreferences: AppPreferences = koinInject()): SwipePreferences {
    val topSwipe by appPreferences.readerOptions.topSwipeGesture.collectChangesWithDefault()
    val bottomSwipe by appPreferences.readerOptions.bottomSwipeGesture.collectChangesWithDefault()

    return SwipePreferences(topSwipe, bottomSwipe)
}

@Stable
private data class SwipePreferences(
    val topSwipe: ArticleVerticalSwipe,
    val bottomSwipe: ArticleVerticalSwipe,
)
