package com.capyreader.app.refresher

import android.content.Context
import com.jocmp.capy.Account
import com.jocmp.capy.common.TimeHelpers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class FeedRefresher(
    private val account: Account,
    applicationContext: Context,
) {
    private val notificationHelper = NotificationHelper(account = account, applicationContext)

    suspend fun refresh() {
        val since = TimeHelpers.nowUTC()

        return withContext(Dispatchers.IO) {
            account.refresh()
            notificationHelper.notify(since = since)
        }
    }
}
