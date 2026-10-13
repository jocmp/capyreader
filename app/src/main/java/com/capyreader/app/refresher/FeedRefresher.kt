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

sealed interface RefreshOutcome {
    data object SkippedWifiOnly : RefreshOutcome

    data class Refreshed(val notifications: Int) : RefreshOutcome

    data class Failed(val error: Throwable, val notifications: Int) : RefreshOutcome
}

class FeedRefresher(
    private val account: Account,
    private val appContext: Context,
    private val notificationHelper: NotificationHelper,
    private val appPreferences: AppPreferences,
): KoinComponent {
    suspend fun refresh(): RefreshOutcome {
        if (appPreferences.refreshOnWiFiOnly.get() && !appContext.isOnWifi()) {
            CapyLog.info("refresh_skipped_mobile", mapOf("type" to "background"))
            return RefreshOutcome.SkippedWifiOnly
        }

        val since = TimeHelpers.nowUTC()

        return withContext(Dispatchers.IO) {
            val result = account.refresh()
            val notifications = notificationHelper.notify(since = since)
            WidgetUpdater.update(appContext)

            val error = result.exceptionOrNull()

            if (error != null) {
                RefreshOutcome.Failed(error = error, notifications = notifications)
            } else {
                RefreshOutcome.Refreshed(notifications = notifications)
            }
        }
    }
}
