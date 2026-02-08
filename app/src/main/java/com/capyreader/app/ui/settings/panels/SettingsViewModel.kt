package com.capyreader.app.ui.settings.panels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.BadgeStyle
import com.jocmp.capy.Account
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val account: Account,
    private val appPreferences: AppPreferences,
) : ViewModel() {
    val source = account.source
    val feeds = account.allFeeds
    val savedSearches = account.savedSearches

    var badgeStyle by mutableStateOf(appPreferences.badgeStyle.get())
        private set

    fun updateBadgeStyle(style: BadgeStyle) {
        badgeStyle = style
        appPreferences.badgeStyle.set(style)

        if (style == BadgeStyle.EXACT) {
            viewModelScope.launch {
                account.toggleAllUnreadBadges(enabled = true)
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

    fun toggleFeedUnreadBadge(feedID: String, enabled: Boolean) {
        viewModelScope.launch {
            account.toggleFeedUnreadBadge(feedID, enabled)
        }
    }

    fun toggleSavedSearchUnreadBadge(id: String, enabled: Boolean) {
        viewModelScope.launch {
            account.toggleSavedSearchUnreadBadge(id, enabled)
        }
    }

    fun selectAllBadges() {
        viewModelScope.launch {
            account.toggleAllUnreadBadges(enabled = true)
        }
    }

    fun selectNoBadges() {
        viewModelScope.launch {
            account.toggleAllUnreadBadges(enabled = false)
        }
    }
}
