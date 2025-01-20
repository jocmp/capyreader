package com.capyreader.app.refresher

import com.capyreader.app.notifications.NotificationHelper
import com.jocmp.capy.Account
import com.jocmp.capy.common.TimeHelpers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

class FeedRefresher(
    private val account: Account,
    private val notificationHelper: NotificationHelper,
): KoinComponent {
    suspend fun refresh() {
        val since = TimeHelpers.nowUTC()

        return withContext(Dispatchers.IO) {
            account.refresh()
            notificationHelper.notify(since = since)
        }
    }
}
