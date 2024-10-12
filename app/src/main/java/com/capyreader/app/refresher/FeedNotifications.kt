package com.capyreader.app.refresher

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.util.Log
import androidx.core.app.NotificationCompat
import com.capyreader.app.Notifications.FEED_UPDATE
import com.capyreader.app.R
import com.capyreader.app.common.notificationManager
import com.jocmp.capy.Account
import com.jocmp.capy.NotifiableFeed
import java.time.ZonedDateTime

class FeedNotifications(
    private val account: Account,
    private val applicationContext: Context,
) {
    private val notificationManager = applicationContext.notificationManager

    suspend fun notify(since: ZonedDateTime) {
        createChannel()

        account.findNotifiableFeeds(since = since).forEach {
            notify(it, since)
        }
    }

    private fun notify(notification: NotifiableFeed, since: ZonedDateTime) {
        val builder = NotificationCompat.Builder(applicationContext, FEED_UPDATE.channelID)
            .setContentTitle(notification.title(applicationContext))
            .setSmallIcon(R.drawable.newsmode)
            .setAutoCancel(true)

        notificationManager.notify(notification.id, builder.build())
    }

    private fun createChannel() {
        val name = applicationContext.getString(R.string.notifications_channel_title_feed_update)
        val channel = NotificationChannel(
            FEED_UPDATE.channelID,
            name,
            NotificationManager.IMPORTANCE_LOW
        )

        notificationManager.createNotificationChannel(channel)
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
