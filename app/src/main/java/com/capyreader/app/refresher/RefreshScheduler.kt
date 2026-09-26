package com.capyreader.app.refresher

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.logging.CapyLog
import java.util.concurrent.TimeUnit

class RefreshScheduler(
    private val context: Context,
    private val appPreferences: AppPreferences,
) {
    val isEnabled: Boolean
        get() = appPreferences.backgroundRefresh.get()

    fun initialize() {
        if (!isEnabled) {
            return
        }

        CapyLog.info("init_refresh")

        enqueue(ExistingPeriodicWorkPolicy.KEEP)
    }

    fun update(enabled: Boolean) {
        if (enabled == isEnabled) {
            return
        }

        appPreferences.backgroundRefresh.set(enabled)

        if (enabled) {
            CapyLog.info("enable_refresh")
            enqueue(ExistingPeriodicWorkPolicy.UPDATE)
        } else {
            CapyLog.info("cancel_refresh")
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
        }
    }

    fun migrateLegacyInterval() {
        val legacyInterval = appPreferences.legacyRefreshInterval

        if (!legacyInterval.isSet()) {
            return
        }

        appPreferences.backgroundRefresh.set(isEnabled)
        legacyInterval.delete()

        if (isEnabled) {
            CapyLog.info("migrate_refresh")
            enqueue(ExistingPeriodicWorkPolicy.UPDATE)
        }
    }

    private fun enqueue(policy: ExistingPeriodicWorkPolicy) {
        val request = PeriodicWorkRequestBuilder<RefreshFeedsWorker>(REFRESH_INTERVAL_HOURS, TimeUnit.HOURS)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            policy,
            request
        )
    }

    private val constraints
        get() = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

    companion object {
        const val WORK_NAME = "refresher"

        private const val REFRESH_INTERVAL_HOURS = 1L
    }
}
