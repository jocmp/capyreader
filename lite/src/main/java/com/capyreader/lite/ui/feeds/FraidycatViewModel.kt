package com.capyreader.lite.ui.feeds

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.capy.Account
import com.jocmp.capy.Article
import com.jocmp.capy.Feed
import com.jocmp.capy.FeedImportance
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ImportanceBucket(
    val importance: FeedImportance,
    val feeds: List<Feed>,
)

class FraidycatViewModel(private val account: Account) : ViewModel() {
    val buckets: StateFlow<List<ImportanceBucket>> = account.allFeeds
        .map { groupByImportance(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedFeedID = MutableStateFlow<String?>(null)
    val selectedFeedID: StateFlow<String?> = _selectedFeedID

    private val _selectedArticles = MutableStateFlow<List<Article>>(emptyList())
    val selectedArticles: StateFlow<List<Article>> = _selectedArticles

    fun selectFeed(id: String) {
        _selectedFeedID.value = id
        viewModelScope.launch(Dispatchers.IO) {
            // TODO: list articles for feed via ArticleRecords; Fraidycat philosophy:
            // include read + unread, sorted newest-first, no status filter.
            _selectedArticles.value = emptyList()
        }
    }

    fun clearSelection() {
        _selectedFeedID.value = null
        _selectedArticles.value = emptyList()
    }

    fun setImportance(feedID: String, importance: FeedImportance) {
        viewModelScope.launch(Dispatchers.IO) {
            account.updateFeedImportance(feedID, importance)
        }
    }

    fun markRead(articleID: String) {
        viewModelScope.launch(Dispatchers.IO) {
            account.markRead(articleID)
        }
    }

    private fun groupByImportance(feeds: List<Feed>): List<ImportanceBucket> {
        val order = listOf(
            FeedImportance.REAL_TIME,
            FeedImportance.DAILY,
            FeedImportance.NORMAL,
            FeedImportance.WEEKLY,
            FeedImportance.MONTHLY,
            FeedImportance.YEARLY,
        )
        val grouped = feeds.groupBy { it.importance }
        return order.mapNotNull { importance ->
            grouped[importance]?.let { ImportanceBucket(importance, it) }
        }
    }
}
