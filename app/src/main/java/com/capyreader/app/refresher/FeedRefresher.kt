package com.capyreader.app.refresher

import android.content.Context
import com.capyreader.app.common.isOnWifi
import com.capyreader.app.notifications.NotificationHelper
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.widget.WidgetUpdater
import com.jocmp.capy.Account
import com.jocmp.capy.common.TimeHelpers
import com.jocmp.capy.logging.CapyLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent

class FeedRefresher(
    private val account: Account,
    private val appContext: Context,
    private val notificationHelper: NotificationHelper,
    private val appPreferences: AppPreferences,
): KoinComponent {
    suspend fun refresh() {
        if (appPreferences.refreshOnWiFiOnly.get() && !appContext.isOnWifi()) {
            CapyLog.info("refresh_skipped_mobile", mapOf("type" to "background"))
            return
        }

        val since = TimeHelpers.nowUTC()

        return withContext(Dispatchers.IO) {
            account.refresh()
            notificationHelper.notify(since = since)
            WidgetUpdater.update(appContext)
        }
    }
}
