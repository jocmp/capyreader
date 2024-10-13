package com.capyreader.app.ui.settings.panels

import androidx.lifecycle.ViewModel
import com.jocmp.capy.Account

class SettingsViewModel(
    private val account: Account,
) : ViewModel() {
    val feeds = account.feeds

    fun toggleNotifications(feedID: String, enabled: Boolean) {
        account.toggleNotifications(feedID = feedID, enabled = enabled)
    }

    fun selectAllFeedNotifications() {
        account.toggleAllFeedNotifications(enabled = true)
    }

    fun deselectAllFeedNotifications() {
        account.toggleAllFeedNotifications(enabled = false)
    }
}
