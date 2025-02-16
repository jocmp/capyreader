package com.capyreader.app.refresher

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.capyreader.app.preferences.AppPreferences

class RefreshScheduler(
    private val context: Context,
    private val appPreferences: AppPreferences,
) {
    val refreshInterval
        get() = appPreferences.refreshInterval.get()

    fun update(interval: RefreshInterval) {
        if (interval == refreshInterval) {
            return
        }

        appPreferences.refreshInterval.set(interval)

        val workManager = WorkManager.getInstance(context)

        val (repeatInterval, timeUnit) = interval.toTime ?: return

        val request = PeriodicWorkRequestBuilder<RefreshFeedsWorker>(repeatInterval, timeUnit)
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    private val constraints
        get() = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()


    companion object {
        const val WORK_NAME = "refresher"
    }
}
