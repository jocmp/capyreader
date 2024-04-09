package com.jocmp.basilreader.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager

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

fun markReadAsync(articleID: String, context: Context) {
    val data = Data
        .Builder()
        .putString(ReadSyncWorker.ARTICLE_KEY, articleID)
        .putBoolean(ReadSyncWorker.MARK_READ_KEY, true)
        .build()

    queueReadWorker(articleID, data, context)
}

fun markUnreadAsync(articleID: String, context: Context) {
    val data = Data
        .Builder()
        .putString(ReadSyncWorker.ARTICLE_KEY, articleID)
        .putBoolean(ReadSyncWorker.MARK_READ_KEY, false)
        .build()

    queueReadWorker(articleID, data, context)
}

private fun queueReadWorker(articleID: String, data: Data, context: Context) {
    val workManager = WorkManager.getInstance(context)

    val request = OneTimeWorkRequestBuilder<ReadSyncWorker>()
        .setConstraints(networkConstraints())
        .setInputData(data)
        .build()

    workManager.enqueueUniqueWork(
        "article_read:${articleID}",
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
