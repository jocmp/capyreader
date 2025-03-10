package com.capyreader.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import com.capyreader.app.common.toast
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.addintent.AddFeedScreen
import com.capyreader.app.ui.collectChangesWithCurrent
import com.capyreader.app.ui.theme.CapyTheme
import org.koin.android.ext.android.inject

class AddFeedActivity : ComponentActivity() {
    val appPreferences by inject<AppPreferences>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val defaultQueryURL = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""

        setContent {
            val theme by appPreferences.theme.collectChangesWithCurrent()

            CapyTheme(theme = theme) {
                AddFeedScreen(
                    onComplete = {
                        popUpToMainActivity()
                    },
                    defaultQueryURL = defaultQueryURL
                )
            }
        }
    }

    private fun popUpToMainActivity() {
        toast(R.string.add_feed_success)

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}
