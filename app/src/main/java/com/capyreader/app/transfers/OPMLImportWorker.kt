package com.capyreader.app.transfers

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
import android.net.Uri
import android.widget.Toast
import androidx.annotation.StringRes
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
import com.capyreader.app.Notifications
import com.capyreader.app.R
import com.capyreader.app.common.notificationManager
import com.jocmp.capy.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import java.util.UUID
import kotlin.math.roundToInt


class OPMLImportWorker(
    context: Context,
    parameters: WorkerParameters
) : CoroutineWorker(context, parameters), KoinComponent {
    private val account by inject<Account>()

    private val channelID = Notifications.OPML_IMPORT.channelID

    private val notificationManager = context.notificationManager

    override suspend fun doWork(): Result {
        val uriValue = inputData.getString(OPML_URI_KEY) ?: return Result.failure()
        val opmlURI = Uri.parse(uriValue)

        setForeground(createForegroundInfo())

        try {
            import(opmlURI)

            showToast(R.string.opml_import_toast_complete)
        } catch (e: Throwable) {
            showToast(R.string.opml_import_toast_failed)
        }

        return Result.success()
    }

    private suspend fun import(opmlUri: Uri) {
        val inputStream = applicationContext.contentResolver.openInputStream(opmlUri)!!

        account.import(inputStream) { progress ->
            val notification = buildNotification(
                percentProgress = (progress.percent * 100f).roundToInt(),
            )

            setProgressAsync(
                workDataOf(
                    PROGRESS_CURRENT_COUNT to progress.currentCount,
                    PROGRESS_TOTAL to progress.total
                )
            )

            setForegroundAsync(
                ForegroundInfo(
                    Notifications.OPML_IMPORT_NOTIFICATION_ID,
                    notification,
                    FOREGROUND_SERVICE_TYPE_DATA_SYNC
                )
            )
        }

        coroutineScope {
            launch {
                account.refresh()
            }
        }
    }

    private fun createForegroundInfo(): ForegroundInfo {
        createChannel()

        val notification = buildNotification(
            percentProgress = 0,
            applicationContext.getString(R.string.opml_import_progress_content_text_start)
        )

        return ForegroundInfo(
            Notifications.OPML_IMPORT_NOTIFICATION_ID,
            notification,
            FOREGROUND_SERVICE_TYPE_DATA_SYNC
        )
    }

    private fun buildNotification(
        percentProgress: Int,
        contentText: String? = null
    ): Notification {
        val title = applicationContext.getString(R.string.opml_import_notification_title)
        val cancelIntent = WorkManager.getInstance(applicationContext).createCancelPendingIntent(id)

        return NotificationCompat.Builder(applicationContext, channelID)
            .setContentTitle(title)
            .setOnlyAlertOnce(true)
            .setContentText(contentText)
            .setProgress(100, percentProgress, false)
            .setSmallIcon(R.drawable.ic_rounded_sync)
            .setTicker(title)
            .addAction(
                R.drawable.ic_rounded_close,
                applicationContext.getString(R.string.opml_import_notification_cancel),
                cancelIntent
            )
            .setOngoing(true)
            .build()
    }

    private fun createChannel() {
        val name = applicationContext.getString(R.string.notifications_channel_title_opml_import)
        val channel = NotificationChannel(channelID, name, NotificationManager.IMPORTANCE_DEFAULT)

        notificationManager.createNotificationChannel(channel)
    }

    private suspend fun showToast(@StringRes string: Int) = withContext(Dispatchers.Main) {
        Toast.makeText(
            applicationContext,
            applicationContext.getString(string),
            Toast.LENGTH_SHORT
        ).show()
    }

    companion object {
        const val PROGRESS_CURRENT_COUNT = "PROGRESS_CURRENT_COUNT"

        const val PROGRESS_TOTAL = "PROGRESS_TOTAL"

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
