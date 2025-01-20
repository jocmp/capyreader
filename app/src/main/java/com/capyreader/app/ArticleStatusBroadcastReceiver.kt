package com.capyreader.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.capyreader.app.notifications.NotificationHelper
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class ArticleStatusBroadcastReceiver : BroadcastReceiver(), KoinComponent {
    private val handler: BroadcastHandler by lazy { BroadcastHandler() }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_DISMISS_NOTIFICATION) {
            val articleID = intent.getStringExtra(ARTICLE_ID) ?: return

            handler.dismissNotification(articleID)
        }
    }

    class BroadcastHandler : KoinComponent {
        private val notificationHelper by inject<NotificationHelper>()

        fun dismissNotification(articleID: String) {
            notificationHelper.dismissNotifications(listOf(articleID))
        }
    }

    companion object {
        const val ACTION_DISMISS_NOTIFICATION = "com.capyreader.ACTION_DISMISS_NOTIFICATION"

        const val ARTICLE_ID = "com.capyreader.articles.article_id"
    }
}
