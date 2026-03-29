package com.jocmp.capyreader.desktop

import com.jocmp.capy.Account
import com.jocmp.capy.Article
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.FeedPriority
import com.jocmp.capy.Folder
import com.jocmp.capy.MarkRead
import com.jocmp.capy.SavedSearch
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.persistence.ArticleRecords
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.OffsetDateTime

class ReaderState(
    val account: Account,
    private val scope: CoroutineScope,
) {
    private val articleRecords = ArticleRecords(account.database)

    private val _filter = MutableStateFlow<ArticleFilter>(ArticleFilter.default())
    val filter: StateFlow<ArticleFilter> = _filter.asStateFlow()

    private val _articles = MutableStateFlow<List<Article>>(emptyList())
    val articles: StateFlow<List<Article>> = _articles.asStateFlow()

    private val _canLoadMore = MutableStateFlow(false)
    val canLoadMore: StateFlow<Boolean> = _canLoadMore.asStateFlow()

    private var currentOffset = 0L

    private val _selectedArticle = MutableStateFlow<Article?>(null)
    val selectedArticle: StateFlow<Article?> = _selectedArticle.asStateFlow()

    private val _refreshing = MutableStateFlow(false)
    val refreshing: StateFlow<Boolean> = _refreshing.asStateFlow()

    val feeds: StateFlow<List<Feed>> = account.feeds
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val folders: StateFlow<List<Folder>> = account.folders
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    val savedSearches: StateFlow<List<SavedSearch>> = account.savedSearches
        .stateIn(scope, SharingStarted.Eagerly, emptyList())

    private val feedCounts: StateFlow<Map<String, Long>> = account.countAll(ArticleStatus.UNREAD)
        .stateIn(scope, SharingStarted.Eagerly, emptyMap())

    val foldersWithCounts: StateFlow<List<Folder>> = combine(folders, feedCounts) { folderList, counts ->
        folderList.map { folder ->
            folder.copy(
                count = folder.feeds.sumOf { counts[it.id] ?: 0L },
                feeds = folder.feeds.map { feed ->
                    feed.copy(count = counts[feed.id] ?: 0L)
                }
            )
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val feedsWithCounts: StateFlow<List<Feed>> = combine(feeds, feedCounts) { feedList, counts ->
        feedList.map { feed ->
            feed.copy(count = counts[feed.id] ?: 0L)
        }
    }.stateIn(scope, SharingStarted.Eagerly, emptyList())

    val allUnreadCount: StateFlow<Long> = account.countAllByStatus(ArticleStatus.UNREAD)
        .stateIn(scope, SharingStarted.Eagerly, 0L)

    fun selectFilter(newFilter: ArticleFilter) {
        _filter.value = newFilter
        _selectedArticle.value = null
        currentOffset = 0
        loadArticles()
    }

    fun selectStatus(status: ArticleStatus) {
        _filter.value = _filter.value.withStatus(status)
        loadArticles()
    }

    fun selectArticle(article: Article) {
        _selectedArticle.value = article
        if (!article.read) {
            scope.launch {
                account.markRead(article.id)
                reloadSelectedArticle()
                loadArticles()
            }
        }
    }

    fun selectNextArticle() {
        val list = _articles.value
        val current = _selectedArticle.value
        if (list.isEmpty()) return

        if (current == null) {
            selectArticle(list.first())
            return
        }

        val index = list.indexOfFirst { it.id == current.id }
        if (index >= 0 && index < list.lastIndex) {
            selectArticle(list[index + 1])
        }
    }

    fun selectPreviousArticle() {
        val list = _articles.value
        val current = _selectedArticle.value ?: return

        val index = list.indexOfFirst { it.id == current.id }
        if (index > 0) {
            selectArticle(list[index - 1])
        }
    }

    fun clearSelection() {
        _selectedArticle.value = null
    }

    fun toggleRead() {
        val article = _selectedArticle.value ?: return
        scope.launch {
            if (article.read) {
                account.markUnread(article.id)
            } else {
                account.markRead(article.id)
            }
            reloadSelectedArticle()
            loadArticles()
        }
    }

    fun toggleStar() {
        val article = _selectedArticle.value ?: return
        scope.launch {
            if (article.starred) {
                account.removeStar(article.id)
            } else {
                account.addStar(article.id)
            }
            reloadSelectedArticle()
            loadArticles()
        }
    }

    fun markAllRead() {
        scope.launch {
            val articleIDs = withContext(Dispatchers.IO) {
                account.unreadArticleIDs(
                    filter = _filter.value,
                    range = MarkRead.All,
                    sortOrder = SortOrder.NEWEST_FIRST,
                    query = null,
                )
            }
            if (articleIDs.isNotEmpty()) {
                account.markAllRead(articleIDs)
                loadArticles()
            }
        }
    }

    fun refresh() {
        if (_refreshing.value) return

        scope.launch {
            _refreshing.value = true
            try {
                withContext(Dispatchers.IO) {
                    account.refresh(_filter.value)
                }
                loadArticles()
            } finally {
                _refreshing.value = false
            }
        }
    }

    fun loadArticles() {
        currentOffset = 0
        scope.launch {
            val result = fetchPage(offset = 0)
            _articles.value = result
            _canLoadMore.value = result.size.toLong() == PAGE_SIZE
        }
    }

    fun loadMore() {
        if (!_canLoadMore.value) return

        scope.launch {
            val nextOffset = currentOffset + PAGE_SIZE
            val result = fetchPage(offset = nextOffset)
            if (result.isNotEmpty()) {
                currentOffset = nextOffset
                _articles.value = _articles.value + result
                _canLoadMore.value = result.size.toLong() == PAGE_SIZE
            } else {
                _canLoadMore.value = false
            }
        }
    }

    private suspend fun fetchPage(offset: Long): List<Article> {
        return withContext(Dispatchers.IO) {
            val currentFilter = _filter.value
            val since = OffsetDateTime.MIN

            when (currentFilter) {
                is ArticleFilter.Articles -> articleRecords.byStatus.all(
                    status = currentFilter.status,
                    limit = PAGE_SIZE,
                    offset = offset,
                    sortOrder = SortOrder.NEWEST_FIRST,
                )
                is ArticleFilter.Feeds -> articleRecords.byFeed.all(
                    feedIDs = listOf(currentFilter.feedID),
                    status = currentFilter.status,
                    since = since,
                    limit = PAGE_SIZE,
                    offset = offset,
                    sortOrder = SortOrder.NEWEST_FIRST,
                    priority = FeedPriority.FEED,
                )
                is ArticleFilter.Folders -> {
                    val feedIDs = account.database.taggingsQueries
                        .findFeedIDs(folderTitle = currentFilter.folderTitle)
                        .executeAsList()
                    articleRecords.byFeed.all(
                        feedIDs = feedIDs,
                        status = currentFilter.status,
                        since = since,
                        limit = PAGE_SIZE,
                        offset = offset,
                        sortOrder = SortOrder.NEWEST_FIRST,
                        priority = FeedPriority.CATEGORY,
                    )
                }
                is ArticleFilter.SavedSearches -> articleRecords.bySavedSearch.all(
                    savedSearchID = currentFilter.savedSearchID,
                    status = currentFilter.status,
                    since = since,
                    limit = PAGE_SIZE,
                    offset = offset,
                    sortOrder = SortOrder.NEWEST_FIRST,
                )
                is ArticleFilter.Today -> articleRecords.byToday.all(
                    status = currentFilter.status,
                    since = null,
                    limit = PAGE_SIZE,
                    offset = offset,
                    sortOrder = SortOrder.NEWEST_FIRST,
                )
            }.executeAsList()
        }
    }

    companion object {
        private const val PAGE_SIZE = 100L
    }

    private suspend fun reloadSelectedArticle() {
        val current = _selectedArticle.value ?: return
        _selectedArticle.value = withContext(Dispatchers.IO) {
            account.findArticle(current.id)
        }
    }
}
