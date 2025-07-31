package com.capyreader.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.capyreader.app.notifications.NotificationHelper
import com.capyreader.app.notifications.NotificationHelper.Companion.ARTICLE_ID_KEY
import com.capyreader.app.ui.widget.WidgetUpdater
import kotlinx.coroutines.launch

class OpenArticleInBrowserActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val articleId = intent.getStringExtra(ARTICLE_ID_KEY)
        val articleUrl = intent.getStringExtra(ARTICLE_URL_KEY)

        if (articleId != null) {
            val markReadIntent = NotificationHelper.markReadIntent(articleId, this)
            sendBroadcast(markReadIntent)
            lifecycleScope.launch {
                WidgetUpdater.update(this@OpenArticleInBrowserActivity)
            }
        }

        if (articleUrl != null) {
            Intent(Intent.ACTION_VIEW)
                .apply {
                    data = articleUrl.toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }.also { intent ->
                    startActivity(intent)
                }
        }

        finish()
    }

    companion object {
        const val ARTICLE_URL_KEY = "article_url"
    }
}