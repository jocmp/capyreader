package com.capyreader.app.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class DeleteNotificationWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams), KoinComponent {
    private val notificationHelper by inject<NotificationHelper>()

    override suspend fun doWork(): Result {
        val notificationID = inputData.getString(ARTICLE_ID) ?: return Result.failure()

        notificationHelper.dismissNotifications(listOf(notificationID))

        return Result.success()
    }

    companion object {
        const val ARTICLE_ID = "article_notification_id"

        fun performAsync(articleID: String, context: Context) {
            val workManager = WorkManager.getInstance(context)
            val data = Data
                .Builder()
                .putString(ARTICLE_ID, articleID)
                .build()

            val request = OneTimeWorkRequestBuilder<DeleteNotificationWorker>()
                .setInputData(data)
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .build()

            workManager.enqueue(request)
        }
    }
}
