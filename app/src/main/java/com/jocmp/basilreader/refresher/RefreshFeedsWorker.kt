package com.jocmp.basilreader.refresher

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.lang.Exception

class RefreshFeedsWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {
    private val refresher by inject<FeedRefresher>()

    override suspend fun doWork(): Result {
        return try {
            refresher.refresh()
            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
