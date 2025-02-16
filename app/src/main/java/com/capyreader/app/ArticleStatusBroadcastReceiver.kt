package com.capyreader.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.capyreader.app.notifications.NotificationHelper
import com.capyreader.app.sync.Sync
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ArticleStatusBroadcastReceiver : BroadcastReceiver(), KoinComponent {
    private val handler: BroadcastHandler by lazy { BroadcastHandler() }

    override fun onReceive(context: Context, intent: Intent) {
        val articleID = intent.getStringExtra(ARTICLE_ID) ?: return

        when (intent.action) {
            ACTION_DISMISS_NOTIFICATION ->
                handler.dismissNotification(articleID)

            ACTION_MARK_AS_READ -> {
                handler.markAsRead(articleID, context = context)
                handler.dismissNotification(articleID)
            }
        }
    }

    class BroadcastHandler : KoinComponent {
        private val notificationHelper by inject<NotificationHelper>()

        fun dismissNotification(articleID: String) {
            notificationHelper.dismissNotifications(listOf(articleID))
        }

        fun markAsRead(articleID: String, context: Context) {
            Sync.markReadAsync(listOf(articleID), context)
        }
    }

    companion object {
        const val ACTION_DISMISS_NOTIFICATION = "com.capyreader.app.ACTION_DISMISS_NOTIFICATION"

        const val ACTION_MARK_AS_READ = "com.capyreader.app.MARK_AS_READ"

        const val ARTICLE_ID = "com.capyreader.app.articles.article_id"
    }
}
