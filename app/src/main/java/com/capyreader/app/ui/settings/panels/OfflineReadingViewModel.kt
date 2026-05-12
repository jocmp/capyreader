package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capyreader.app.common.OfflineStorage
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class OfflineReadingViewModel(
    private val account: Account,
    private val appPreferences: AppPreferences,
    private val offlineStorage: OfflineStorage,
) : ViewModel() {
    var offlineStarredArticles by mutableStateOf(appPreferences.offlineStarredArticles.get())
        private set

    val cacheLimitBytes: Long = offlineStorage.limitBytes()

    var cacheUsedBytes by mutableStateOf(offlineStorage.usedBytes(account))
        private set

    init {
        viewModelScope.launch(Dispatchers.IO) {
            cacheUsedBytes = offlineStorage.usedBytes(account)
        }
    }

    fun updateOfflineStarredArticles(enabled: Boolean) {
        appPreferences.offlineStarredArticles.set(enabled)
        offlineStarredArticles = enabled
    }

    fun clearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            account.clearOfflineCache()
            cacheUsedBytes = offlineStorage.usedBytes(account)
        }
    }
}
