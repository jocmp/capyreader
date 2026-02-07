package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.CountStyle
import com.jocmp.capy.Account
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val account: Account,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    val source = account.source
    val feeds = account.allFeeds
    val savedSearches = account.savedSearches

    var countStyle by mutableStateOf(appPreferences.countStyle.get())
        private set

    fun updateCountStyle(style: CountStyle) {
        countStyle = style
        appPreferences.countStyle.set(style)

        if (style == CountStyle.EXACT) {
            viewModelScope.launch {
                account.toggleAllShowUnreadCounts(enabled = true)
            }
        }
    }

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

    fun toggleFeedShowUnreadCounts(feedID: String, enabled: Boolean) {
        viewModelScope.launch {
            account.toggleFeedShowUnreadCounts(feedID, enabled)
        }
    }

    fun toggleSavedSearchShowUnreadCounts(id: String, enabled: Boolean) {
        viewModelScope.launch {
            account.toggleSavedSearchShowUnreadCounts(id, enabled)
        }
    }

    fun selectAllBadges() {
        viewModelScope.launch {
            account.toggleAllShowUnreadCounts(enabled = true)
        }
    }

    fun selectNoBadges() {
        viewModelScope.launch {
            account.toggleAllShowUnreadCounts(enabled = false)
        }
    }
}
