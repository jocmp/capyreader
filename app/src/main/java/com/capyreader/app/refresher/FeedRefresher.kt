package com.capyreader.app.refresher

import android.content.Context
import com.capyreader.app.common.OfflineStorage
import com.capyreader.app.notifications.NotificationHelper
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.widget.WidgetUpdater
import com.jocmp.capy.Account
import com.jocmp.capy.common.TimeHelpers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

class FeedRefresher(
    private val account: Account,
    private val appContext: Context,
    private val notificationHelper: NotificationHelper,
    private val appPreferences: AppPreferences,
    private val offlineStorage: OfflineStorage,
): KoinComponent {
    suspend fun refresh() {
        val since = TimeHelpers.nowUTC()

        return withContext(Dispatchers.IO) {
            account.refresh()
            if (appPreferences.offlineStarredArticles.get()) {
                account.downloadStarredArticles(limitBytes = offlineStorage.limitBytes())
            }
            notificationHelper.notify(since = since)
            WidgetUpdater.update(appContext)
        }
    }
}
