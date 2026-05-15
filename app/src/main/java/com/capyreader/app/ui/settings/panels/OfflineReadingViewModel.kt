package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.capy.Account
import com.jocmp.capy.Feed
import com.jocmp.capy.common.sortedByTitle
import com.jocmp.capy.preferences.OfflineCacheSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OfflineReadingViewModel(
    private val account: Account,
) : ViewModel() {
    var cacheSize: OfflineCacheSize by mutableStateOf(account.preferences.offlineCacheSize())
        private set

    val feeds: Flow<List<Feed>> = account.allFeeds.map { it.sortedByTitle() }

    fun updateCacheSize(size: OfflineCacheSize) {
        account.preferences.setOfflineCacheSize(size)
        cacheSize = size
    }

    fun toggleFeedOffline(feedID: String, enabled: Boolean) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                account.updateFeedOfflineEnabled(feedID = feedID, enabled = enabled)
            }
        }
    }
}
