package com.jocmp.capyreader.transfers

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.net.Uri
import androidx.core.app.NotificationCompat
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ForegroundInfo
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.jocmp.capy.Account
import com.jocmp.capy.opml.ImportProgress
import com.jocmp.capyreader.Notifications
import com.jocmp.capyreader.R
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID

private const val TAG = "OPMLImportWorker"

class OPMLImportWorker(
    context: Context,
    parameters: WorkerParameters
) : CoroutineWorker(context, parameters), KoinComponent {
    private val account by inject<Account>()


    private val channelID = Notifications.OPML_IMPORT.channelID
    private val notificationsID = Notifications.OPML_IMPORT.notificationID

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager

    override suspend fun doWork(): Result {
        val uriValue = inputData.getString(OPML_URI_KEY) ?: return Result.failure()
        val opmlURI = Uri.parse(uriValue)

        setForeground(createForegroundInfo())
        import(opmlURI)

        return Result.success()
    }

    private suspend fun import(opmlUri: Uri) {
        val inputStream = applicationContext.contentResolver.openInputStream(opmlUri)!!

        account.import(inputStream) { progress ->
            val notification = buildNotification(
                percentProgress = progress.percent,
                applicationContext.getString(R.string.opml_import_progress_content_text_progress)
            )

            setProgressAsync(workDataOf(PROGRESS to progress.percent))
            setForegroundAsync(
                ForegroundInfo(
                    notificationsID,
                    notification,
                    FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            )
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        createChannel()

        val notification = buildNotification(
            percentProgress = 0,
            applicationContext.getString(R.string.opml_import_progress_content_text_start)
        )

        return ForegroundInfo(notificationsID, notification, FOREGROUND_SERVICE_TYPE_DATA_SYNC)
    }

    private fun buildNotification(
        percentProgress: Int,
        contentText: String
    ): android.app.Notification {
        val title = applicationContext.getString(R.string.opml_import_notification_title)

        return NotificationCompat.Builder(applicationContext, channelID)
            .setContentTitle(title)
            .setContentText(contentText)
            .setProgress(100, percentProgress, false)
            .setSmallIcon(R.drawable.ic_rss_feed)
            .setTicker(title)
            .setOngoing(true)
            .build()
    }

    private fun createChannel() {
        val name = applicationContext.getString(R.string.notifications_channel_title)
        val channel = NotificationChannel(channelID, name, NotificationManager.IMPORTANCE_DEFAULT)

        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val PROGRESS = "import_progress"

        private const val OPML_URI_KEY = "OPML_URI_KEY"

        private const val WORK_NAME = "OPML_IMPORT"

        fun performAsync(context: Context, uri: Uri): UUID {
            val request = OneTimeWorkRequestBuilder<OPMLImportWorker>()
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .setInputData(
                    Data
                        .Builder()
                        .putString(OPML_URI_KEY, uri.toString())
                        .build()
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )

            return request.id
        }
    }
}
