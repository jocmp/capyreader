package com.capyreader.app.refresher

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.Style
import com.capyreader.app.MainActivity
import com.capyreader.app.Notifications
import com.capyreader.app.Notifications.FEED_UPDATE
import com.capyreader.app.R
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.notificationManager
import com.capyreader.app.refresher.NotificationHelper.Companion.ARTICLE_ID_KEY
import com.capyreader.app.refresher.NotificationHelper.Companion.FEED_ID_KEY
import com.jocmp.capy.Account
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.notifications.ArticleNotification
import java.time.ZonedDateTime

class NotificationHelper(
    private val account: Account,
    private val applicationContext: Context,
) {
    private val notificationManager = applicationContext.notificationManager

    suspend fun notify(since: ZonedDateTime) {
        createChannel()

        val notifications = account.findNotifications(since = since)

        if (notifications.isEmpty()) {
            return
        }

        notifications.forEach {
            sendNotification(it)
        }

        sendGroupNotification(notifications)
    }

    private fun sendNotification(notification: ArticleNotification) {
        val builder = NotificationCompat.Builder(applicationContext, FEED_UPDATE.channelID)
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
            .setContentIntent(notification.intent(applicationContext))

        notificationManager.notify(notification.notificationID, builder.build())
    }

    private fun sendGroupNotification(notifications: List<ArticleNotification>) {
        val builder = NotificationCompat.Builder(applicationContext, FEED_UPDATE.channelID)
            .setContentTitle("${notifications.size} new notifications")
            .setSmallIcon(R.drawable.newsmode)
            .setGroup(ARTICLE_REFRESH_GROUP)
            .setGroupSummary(true)
            .setStyle(groupInboxStyle(notifications))

        notificationManager.notify(Notifications.FEED_UPDATE_GROUP_NOTIFICATION_ID, builder.build())
    }

    private fun createChannel() {
        val name = applicationContext.getString(R.string.notifications_channel_title_feed_update)
        val channel = NotificationChannel(
            FEED_UPDATE.channelID,
            name,
            NotificationManager.IMPORTANCE_DEFAULT
        )

        notificationManager.createNotificationChannel(channel)
    }

    companion object {
        const val ARTICLE_ID_KEY = "article_id"
        const val FEED_ID_KEY = "feed_id"
        private const val ARTICLE_REFRESH_GROUP = "article_refresh"

        fun handleResult(intent: Intent, appPreferences: AppPreferences) {
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

private fun groupInboxStyle(notifications: List<ArticleNotification>): Style {
    val style = NotificationCompat.InboxStyle()

    notifications.take(3).forEach {
        style.addLine(it.title)
    }

//    style.setSummaryText(title)

    return style
}


private fun ArticleNotification.intent(context: Context): PendingIntent {
    val notifyIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra(ARTICLE_ID_KEY, id)
        putExtra(FEED_ID_KEY, feedID)
    }

    return PendingIntent.getActivity(
        context,
        notificationID,
        notifyIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

private fun List<ArticleNotification>.grouped() =
    groupBy { it.feedID }.map { (feedID, notifications) ->
        FeedNotification.from(feedID, notifications)
    }