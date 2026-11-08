package com.capyreader.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.capyreader.app.common.openLink
import com.capyreader.app.notifications.NotificationHelper
import com.capyreader.app.notifications.NotificationHelper.Companion.ARTICLE_ID_KEY
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.widget.WidgetUpdater
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class OpenArticleInBrowserActivity : AppCompatActivity() {
    private val appPreferences by inject<AppPreferences>()

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
            openLink(
                context = this,
                url = articleUrl.toUri(),
                openExternalAdjacent = false,
                openInternally = appPreferences.openLinksInternally.get(),
            )
        }

        finish()
    }

    companion object {
        const val ARTICLE_URL_KEY = "article_url"
    }
}
