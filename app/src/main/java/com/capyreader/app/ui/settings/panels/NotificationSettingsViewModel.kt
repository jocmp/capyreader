package com.capyreader.app.ui.settings.panels

import androidx.lifecycle.ViewModel
import com.jocmp.capy.Account

class NotificationSettingsViewModel(
    account: Account,
): ViewModel() {
    val feeds = account.feeds
}