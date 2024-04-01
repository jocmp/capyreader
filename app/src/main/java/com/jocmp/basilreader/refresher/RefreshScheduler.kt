package com.jocmp.basilreader.refresher

import android.content.Context
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.jocmp.basilreader.common.AppPreferences

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
