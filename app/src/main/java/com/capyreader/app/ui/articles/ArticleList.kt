package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.paging.PagingData
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.articles.list.ArticleListScrollState
import com.capyreader.app.ui.articles.list.ArticleRecyclerView
import com.jocmp.capy.Article
import com.jocmp.capy.MarkRead
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import org.koin.compose.koinInject
import java.time.LocalDateTime

@Composable
fun ArticleList(
    articles: Flow<PagingData<Article>>,
    listKey: String,
    onSelect: (articleID: String) -> Unit,
    selectedArticleKey: String?,
    scrollState: ArticleListScrollState,
    onMarkAllRead: (range: MarkRead) -> Unit = {},
    refreshingAll: Boolean,
    enableMarkReadOnScroll: Boolean = false,
) {
    ArticleRecyclerView(
        articles = articles,
        listKey = listKey,
        selectedArticleKey = selectedArticleKey,
        onSelect = onSelect,
        onMarkAllRead = onMarkAllRead,
        enableMarkReadOnScroll = enableMarkReadOnScroll,
        refreshingAll = refreshingAll,
        scrollState = scrollState,
    )
}

@Composable
fun rememberCurrentTime(): LocalDateTime {
    var currentTime by remember { mutableStateOf(LocalDateTime.now()) }

    LaunchedEffect(Unit) {
        while (true) {
            currentTime = LocalDateTime.now()
            delay(30 * 60 * 1_000)
        }
    }

    return currentTime
}

@Composable
fun rememberArticleOptions(appPreferences: AppPreferences = koinInject()): ArticleRowOptions {
    val scope = rememberCoroutineScope()

    val showSummary by appPreferences.articleListOptions.showSummary.stateIn(scope).collectAsState()
    val showIcon by appPreferences.articleListOptions.showFeedIcons.stateIn(scope).collectAsState()
    val showFeedName by appPreferences.articleListOptions.showFeedName.stateIn(scope)
        .collectAsState()
    val imagePreview by appPreferences.articleListOptions.imagePreview.stateIn(scope)
        .collectAsState()
    val fontScale by appPreferences.articleListOptions.fontScale.stateIn(scope).collectAsState()
    val shortenTitles by appPreferences.articleListOptions.shortenTitles.stateIn(scope)
        .collectAsState()
    val showAudioIcon by appPreferences.enableAudioPlayer.stateIn(scope).collectAsState()

    return ArticleRowOptions(
        showSummary = showSummary,
        showIcon = showIcon,
        showFeedName = showFeedName,
        imagePreview = imagePreview,
        fontScale = fontScale,
        shortenTitles = shortenTitles,
        showAudioIcon = showAudioIcon,
    )
}
