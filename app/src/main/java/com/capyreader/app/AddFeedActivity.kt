package com.capyreader.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import com.capyreader.app.common.toast
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.addintent.AddFeedScreen
import com.capyreader.app.ui.collectChangesWithCurrent
import com.capyreader.app.ui.theme.CapyTheme
import org.koin.android.ext.android.inject

class AddFeedActivity : BaseActivity() {
    val appPreferences by inject<AppPreferences>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (appPreferences.accountID.get().isBlank()) {
            popUpToMainActivity()
            return
        }

        val defaultQueryURL = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""

        setContent {
            val themeMode by appPreferences.themeMode.collectChangesWithCurrent()
            val appTheme by appPreferences.appTheme.collectChangesWithCurrent()
            val pureBlackDarkMode by appPreferences.pureBlackDarkMode.collectChangesWithCurrent()

            CapyTheme(themeMode = themeMode, appTheme = appTheme, pureBlack = pureBlackDarkMode) {
                AddFeedScreen(
                    defaultQueryURL = defaultQueryURL,
                    onComplete = {
                        toast(R.string.add_feed_success)
                        popUpToMainActivity()
                    },
                    onBack = {
                        popUpToMainActivity()
                    }
                )
            }
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    private fun popUpToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}
