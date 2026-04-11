package com.capyreader.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.capyreader.app.notifications.NotificationHelper
import com.capyreader.app.notifications.NotificationIntent
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.App
import com.capyreader.app.ui.Route
import org.koin.android.ext.android.get

class MainActivity : BaseActivity() {
    private var pendingNotification by mutableStateOf(NotificationIntent())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingNotification = NotificationHelper.openFromIntent(intent)

        setContent {
            App(
                startDestination = startDestination(),
                pendingArticleID = pendingNotification.articleID,
                pendingFilter = pendingNotification.filter,
                onPendingNotificationHandled = { pendingNotification = NotificationIntent() },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingNotification = NotificationHelper.openFromIntent(intent)
    }

    private fun startDestination(): Route {
        val appPreferences = get<AppPreferences>()

        val accountID = appPreferences.accountID.get()

        return if (accountID.isBlank()) {
            Route.AddAccount
        } else {
            Route.Articles
        }
    }
}
