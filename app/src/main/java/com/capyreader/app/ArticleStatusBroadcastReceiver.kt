package com.capyreader.app

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.capyreader.app.sync.Sync

class ArticleStatusBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == ACTION_CLEAR_NOTIFICATION) {
            val articleID = intent.getIntExtra(ARTICLE_NOTIFICATION_ID, 0)

            if (articleID > 0) {

            }

            Sync.markReadAsync(listOf(articleID), context)
        }
    }

    companion object {
        const val ACTION_CLEAR_NOTIFICATION = "com.capyreader.ACTION_CLEAR_NOTIFICATION"

        const val ARTICLE_NOTIFICATION_ID = "com.capyreader.articles.article_notification_id"
    }
}
