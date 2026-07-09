package com.capyreader.app

import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.capyreader.app.common.VolumeKeyPager
import com.capyreader.app.notifications.NotificationHelper
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.App
import com.capyreader.app.ui.Route
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject

class MainActivity : BaseActivity() {
    val appPreferences by inject<AppPreferences>()

    private var pendingArticleID by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        pendingArticleID = NotificationHelper.openFromIntent(intent, appPreferences = appPreferences)

        setContent {
            App(
                startDestination = startDestination(),
                appPreferences = appPreferences,
                pendingArticleID = pendingArticleID,
                onPendingArticleSelected = { pendingArticleID = null },
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        pendingArticleID = NotificationHelper.openFromIntent(intent, appPreferences = appPreferences)
    }

    private var lastVolumeUpDownTime = 0L

    override fun dispatchKeyEvent(event: KeyEvent): Boolean {
        val keyCode = event.keyCode
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            // Only hijack volume keys while a reader is open, the setting is on,
            // and no audio is playing; otherwise fall through to normal volume.
            if (VolumeKeyPager.canHandle()) {
                if (event.action == KeyEvent.ACTION_DOWN) {
                    // Double-click volume-up acts as the Back button. Only
                    // discrete presses (repeatCount 0) count, so holding to page
                    // never misfires; volume-down never triggers back, so paging
                    // forward through an article is always safe. Dispatching a
                    // real back press means it honors the user's configured back
                    // action — close the article in the reader, or open the
                    // navigation drawer / navigate to parent in the list.
                    if (keyCode == KeyEvent.KEYCODE_VOLUME_UP && event.repeatCount == 0) {
                        val now = event.eventTime
                        if (now - lastVolumeUpDownTime <= DOUBLE_CLICK_MS) {
                            lastVolumeUpDownTime = 0L
                            onBackPressedDispatcher.onBackPressed()
                            return true
                        }
                        lastVolumeUpDownTime = now
                    }
                    VolumeKeyPager.handle(forward = keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)
                }
                // Consume both DOWN and UP so the system volume UI never shows.
                return true
            }
        }
        return super.dispatchKeyEvent(event)
    }

    companion object {
        private const val DOUBLE_CLICK_MS = 320L
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
