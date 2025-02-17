package com.capyreader.app.notifications

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.capyreader.app.ArticleStatusBroadcastReceiver
import com.capyreader.app.MainActivity
import com.capyreader.app.R
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.notifications.NotificationHelper.Companion.ARTICLE_ID_KEY
import com.capyreader.app.notifications.NotificationHelper.Companion.FEED_ID_KEY
import com.jocmp.capy.Account
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleNotification
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.logging.CapyLog
import java.time.ZonedDateTime

class NotificationHelper(
    private val account: Account,
    private val applicationContext: Context,
) {
    suspend fun notify(since: ZonedDateTime) {
        createChannel()

        val notifications = account.createNotifications(since = since)

        if (notifications.isEmpty()) {
            return
        }

        sendGroupNotification()

        notifications.forEach {
            sendNotification(it)
        }
    }

    fun dismissNotifications(ids: List<String>) {
        account.dismissNotifications(ids)

        val notificationManager = NotificationManagerCompat.from(applicationContext)

        ids.forEach {
            notificationManager.cancel(it.hashCode())
        }

        if (account.countActiveNotifications() == 0L) {
            notificationManager.cancel(Notifications.FEED_UPDATE_GROUP_NOTIFICATION_ID)
        }
    }

    private fun sendNotification(notification: ArticleNotification) {
        val clearNotificationIntent = PendingIntent.getBroadcast(
            applicationContext,
            notification.id,
            dismissNotificationIntent(notification.articleID, context = applicationContext),
            PendingIntent.FLAG_IMMUTABLE
        )

        val markReadIntent = PendingIntent.getBroadcast(
            applicationContext,
            notification.id,
            markReadIntent(notification.articleID, context = applicationContext),
            PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(
            applicationContext,
            Notifications.FEED_UPDATE.channelID
        )
            .setContentText(notification.title)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(notification.title)
            )
            .setSmallIcon(R.drawable.newsmode)
            .setGroup(ARTICLE_REFRESH_GROUP)
            .setSubText(notification.feedTitle)
            .setContentInfo(notification.title)
            .setAutoCancel(true)
            .setDeleteIntent(clearNotificationIntent)
            .setContentIntent(notification.contentIntent(applicationContext))
            .addAction(
                R.drawable.icon_circle_filled,
                applicationContext.getString(R.string.notification_mark_as_read_action),
                markReadIntent,
            )

        NotificationManagerCompat.from(applicationContext)
            .tryNotify(notification.id, builder.build())
    }

    private fun sendGroupNotification() {
        val builder = NotificationCompat.Builder(
            applicationContext,
            Notifications.FEED_UPDATE.channelID
        )
            .setSmallIcon(R.drawable.newsmode)
            .setGroup(ARTICLE_REFRESH_GROUP)
            .setGroupSummary(true)

        NotificationManagerCompat.from(applicationContext)
            .tryNotify(Notifications.FEED_UPDATE_GROUP_NOTIFICATION_ID, builder.build())
    }

    private fun createChannel() {
        val name = applicationContext.getString(R.string.notifications_channel_title_feed_update)
        val channel = NotificationChannel(
            Notifications.FEED_UPDATE.channelID,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )

        NotificationManagerCompat.from(applicationContext)
            .createNotificationChannel(channel)
    }

    companion object {
        const val ARTICLE_ID_KEY = "article_id"
        const val FEED_ID_KEY = "feed_id"
        private const val ARTICLE_REFRESH_GROUP = "article_refresh"

        fun dismissNotificationIntent(articleID: String, context: Context): Intent {
            return Intent(context, ArticleStatusBroadcastReceiver::class.java).apply {
                action = ArticleStatusBroadcastReceiver.ACTION_DISMISS_NOTIFICATION
                putExtra(ArticleStatusBroadcastReceiver.ARTICLE_ID, articleID)
            }
        }

        fun markReadIntent(articleID: String, context: Context): Intent {
            return Intent(context, ArticleStatusBroadcastReceiver::class.java).apply {
                action = ArticleStatusBroadcastReceiver.ACTION_MARK_AS_READ
                putExtra(ArticleStatusBroadcastReceiver.ARTICLE_ID, articleID)
            }
        }

        fun openArticle(intent: Intent, appPreferences: AppPreferences) {
            val articleID = intent.getStringExtra(ARTICLE_ID_KEY) ?: return
            val feedID = intent.getStringExtra(FEED_ID_KEY) ?: return
            intent.replaceExtras(Bundle())

            appPreferences.filter.set(
                ArticleFilter.Feeds(
                    feedID,
                    feedStatus = ArticleStatus.UNREAD,
                    folderTitle = null
                )
            )
            appPreferences.articleID.set(articleID)
        }
    }
}

private fun NotificationManagerCompat.tryNotify(id: Int, notification: Notification) {
    try {
        notify(id, notification)
    } catch (e: SecurityException) {
        CapyLog.error("notification_helper", e)
    }
}

private fun ArticleNotification.contentIntent(context: Context): PendingIntent {
    val notifyIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra(ARTICLE_ID_KEY, articleID)
        putExtra(FEED_ID_KEY, feedID)
    }

    return PendingIntent.getActivity(
        context,
        id,
        notifyIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
