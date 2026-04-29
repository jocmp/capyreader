package com.capyreader.app.refresher

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.jocmp.capy.logging.CapyLog
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class RefreshFeedsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {
    private val refresher by inject<FeedRefresher>()

    override suspend fun doWork(): Result {
        CapyLog.info("refresh_feeds_worker:start")
        return try {
            refresher.refresh()
            CapyLog.info("refresh_feeds_worker:success")
            Result.success()
        } catch (e: Exception) {
            CapyLog.error("refresh_feeds_worker", e)
            Result.failure()
        }
    }
}
