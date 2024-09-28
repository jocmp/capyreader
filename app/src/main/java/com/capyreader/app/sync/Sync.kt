package com.capyreader.app.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.capyreader.app.common.MD5

object Sync {
    fun addStarAsync(articleID: String, context: Context) {
        val data = Data
            .Builder()
            .putString(StarSyncWorker.ARTICLE_KEY, articleID)
            .putBoolean(StarSyncWorker.ADD_STAR_KEY, true)
            .build()

        queueStarWorker(articleID, data, context)
    }

    fun removeStarAsync(articleID: String, context: Context) {
        val data = Data
            .Builder()
            .putString(StarSyncWorker.ARTICLE_KEY, articleID)
            .putBoolean(StarSyncWorker.ADD_STAR_KEY, false)
            .build()

        queueStarWorker(articleID, data, context)
    }

    /**
     * Batch enqueue IDs to stay under 10KB work manager limit
     * https://developer.android.com/jetpack/androidx/releases/work#1.0.0-alpha08
     */
    fun markReadAsync(articleIDs: List<String>, context: Context, batchSize: Int = 100) {
        if (batchSize < 1) {
            return
        }

        articleIDs.chunked(batchSize) { batch ->
            try {
                val data = Data
                    .Builder()
                    .putStringArray(ReadSyncWorker.ARTICLES_KEY, batch.toTypedArray())
                    .putBoolean(ReadSyncWorker.MARK_READ_KEY, true)
                    .build()

                queueReadWorker(batch, data, context)
            } catch (e: IllegalStateException) {
                markReadAsync(context = context, articleIDs = batch, batchSize = batchSize / 2)
            }
        }
    }

    fun markUnreadAsync(articleID: String, context: Context) {
        val articleIDs = listOf(articleID)

        val data = Data
            .Builder()
            .putStringArray(ReadSyncWorker.ARTICLES_KEY, articleIDs.toTypedArray())
            .putBoolean(ReadSyncWorker.MARK_READ_KEY, false)
            .build()

        queueReadWorker(articleIDs, data, context)
    }
}


private fun queueReadWorker(articleIDs: List<String>, data: Data, context: Context) {
    val workManager = WorkManager.getInstance(context)

    val request = OneTimeWorkRequestBuilder<ReadSyncWorker>()
        .setConstraints(networkConstraints())
        .setInputData(data)
        .build()

    val key = MD5.from(articleIDs.joinToString(","))

    workManager.enqueueUniqueWork(
        "article_read:$key",
        ExistingWorkPolicy.REPLACE,
        request
    )
}

private fun queueStarWorker(articleID: String, data: Data, context: Context) {
    val workManager = WorkManager.getInstance(context)

    val request = OneTimeWorkRequestBuilder<StarSyncWorker>()
        .setConstraints(networkConstraints())
        .setInputData(data)
        .build()

    workManager.enqueueUniqueWork(
        "article_star:${articleID}",
        ExistingWorkPolicy.REPLACE,
        request
    )
}

private fun networkConstraints() =
    Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .build()
