package com.capyreader.app.ui.settings.panels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jocmp.capy.Account
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val account: Account,
) : ViewModel() {
    val feeds = account.allFeeds

    fun toggleNotifications(feedID: String, enabled: Boolean) {
        viewModelScope.launch {
            account.toggleNotifications(feedID = feedID, enabled = enabled)
        }
    }

    fun selectAllFeedNotifications() {
        viewModelScope.launch {
            account.toggleAllFeedNotifications(enabled = true)
        }
    }

    fun deselectAllFeedNotifications() {
        viewModelScope.launch {
            account.toggleAllFeedNotifications(enabled = false)
        }
    }
}
