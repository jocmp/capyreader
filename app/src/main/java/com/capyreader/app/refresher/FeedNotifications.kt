package com.capyreader.app.refresher

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.capyreader.app.MainActivity
import com.capyreader.app.Notifications.FEED_UPDATE
import com.capyreader.app.R
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.common.notificationManager
import com.capyreader.app.refresher.FeedNotifications.Companion.FEED_ID_KEY
import com.jocmp.capy.Account
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.NotifiableFeed
import com.jocmp.capy.preferences.getAndSet
import java.time.ZonedDateTime

class FeedNotifications(
    private val account: Account,
    private val applicationContext: Context,
) {
    private val notificationManager = applicationContext.notificationManager

    suspend fun notify(since: ZonedDateTime) {
        createChannel()

        account.findNotifiableFeeds(since = since).forEach {
            notify(it)
        }
    }

    private fun notify(notification: NotifiableFeed) {
        val builder = NotificationCompat.Builder(applicationContext, FEED_UPDATE.channelID)
            .setContentTitle(notification.title(applicationContext))
            .setSmallIcon(R.drawable.newsmode)
            .setContentIntent(notification.intent(applicationContext))
            .setAutoCancel(true)

        notificationManager.notify(notification.id, builder.build())
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
        const val FEED_ID_KEY = "feed_id"

        fun handleResult(intent: Intent, appPreferences: AppPreferences) {
            val feedID = intent.getStringExtra(FEED_ID_KEY) ?: return

            appPreferences.filter.set(
                ArticleFilter.Feeds(feedID = feedID, feedStatus = ArticleStatus.UNREAD)
            )
        }
    }
}

private fun NotifiableFeed.title(context: Context): String {
    return context.resources.getQuantityString(
        R.plurals.notifications_feed_update_title,
        articleCount.toInt(),
        articleCount.toInt(),
        feed.title
    )
}

private fun NotifiableFeed.intent(context: Context): PendingIntent {
    val notifyIntent = Intent(context, MainActivity::class.java).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        putExtra(FEED_ID_KEY, feed.id)
    }

    return PendingIntent.getActivity(
        context,
        id,
        notifyIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}
