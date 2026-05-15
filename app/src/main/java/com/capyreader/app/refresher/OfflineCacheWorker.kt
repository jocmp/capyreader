package com.capyreader.app.refresher

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.jocmp.capy.Account
import com.jocmp.capy.logging.CapyLog
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OfflineCacheWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), KoinComponent {
    private val account by inject<Account>()

    override suspend fun doWork(): Result {
        CapyLog.info("offline_cache_worker:start")
        return try {
            account.runOfflineCacheSync()
            CapyLog.info("offline_cache_worker:success")
            Result.success()
        } catch (e: Exception) {
            CapyLog.error("offline_cache_worker", e)
            Result.failure()
        }
    }

    companion object {
        const val UNIQUE_NAME = "offline_cache_worker"

        fun enqueue(context: Context) {
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_NAME,
                ExistingWorkPolicy.KEEP,
                OneTimeWorkRequestBuilder<OfflineCacheWorker>().build(),
            )
        }
    }
}
