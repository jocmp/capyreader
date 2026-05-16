package com.capyreader.app.refresher

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.OFFLINE_CACHE_BUFFER_BYTES
import com.capyreader.app.preferences.OFFLINE_PER_ASSET_MAX_BYTES
import com.capyreader.app.preferences.OFFLINE_RETRY_AFTER_SECONDS
import com.jocmp.capy.Account
import com.jocmp.capy.logging.CapyLog
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class OfflineDownloadWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams), KoinComponent {
    private val account by inject<Account>()
    private val appPreferences by inject<AppPreferences>()

    override suspend fun doWork(): Result {
        CapyLog.info("offline_worker_start")
        val result = account.downloadOfflineQueue(
            maxArticles = MAX_ARTICLES_PER_RUN,
            cacheLimitBytes = appPreferences.offlineCacheLimitBytes.get(),
            cacheBufferBytes = OFFLINE_CACHE_BUFFER_BYTES,
            perAssetMaxBytes = OFFLINE_PER_ASSET_MAX_BYTES,
            retryAfterSeconds = OFFLINE_RETRY_AFTER_SECONDS,
        )
        return result.fold(
            onSuccess = {
                CapyLog.info("offline_worker_success", mapOf("processed" to it.toString()))
                Result.success()
            },
            onFailure = {
                CapyLog.error("offline_worker", it)
                Result.retry()
            },
        )
    }

    companion object {
        const val WORK_NAME = "offline_download"
        const val MAX_ARTICLES_PER_RUN = 50

        fun enqueue(context: Context) {
            val request = OneTimeWorkRequestBuilder<OfflineDownloadWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.KEEP,
                request,
            )
        }
    }
}
