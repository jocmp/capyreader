package com.jocmp.basilreader.refresher

import android.content.Context
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.jocmp.basilreader.common.AppPreferences

class RefreshScheduler(
    private val context: Context,
    private val appPreferences: AppPreferences,
) {
    fun update(interval: RefreshInterval) {
        val previousInterval = appPreferences.refreshInterval.get()

        if (interval == previousInterval) {
            return
        }

        appPreferences.refreshInterval.set(interval)

        val workManager = WorkManager.getInstance(context)

        workManager.cancelAllWorkByTag(WORK_TAG)

        val (repeatInterval, timeUnit) = interval.toTime ?: return

        val request = PeriodicWorkRequestBuilder<RefreshFeedsWorker>(repeatInterval, timeUnit)
            .addTag(WORK_TAG)
            .build()

        workManager.enqueue(request)
    }

    companion object {
        const val WORK_TAG = "refresher"
    }
}
