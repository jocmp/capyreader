package com.capyreader.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.addintent.AddLinkScreen
import com.capyreader.app.ui.collectChangesWithCurrent
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.Account
import org.koin.android.ext.android.inject

class AddLinkActivity : BaseActivity() {
    val appPreferences by inject<AppPreferences>()
    val account by inject<Account>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (appPreferences.accountID.get().isBlank()) {
            startMainActivity()
            return
        }

        val defaultQueryURL = intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        val pageTitle = intent.getStringExtra(Intent.EXTRA_SUBJECT).orEmpty()

        setContent {
            val themeMode by appPreferences.themeMode.collectChangesWithCurrent()
            val appTheme by appPreferences.appTheme.collectChangesWithCurrent()
            val pureBlackDarkMode by appPreferences.pureBlackDarkMode.collectChangesWithCurrent()

            CapyTheme(themeMode = themeMode, appTheme = appTheme, pureBlack = pureBlackDarkMode) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center,
                ) {
                    Box(
                        modifier = Modifier.preferredMaxWidth(),
                    ) {
                        AddLinkScreen(
                            defaultQueryURL = defaultQueryURL,
                            pageTitle = pageTitle,
                            supportsPages = account.source.supportsPages,
                            onBack = {
                                finish()
                            }
                        )
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        finish()
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        startActivity(intent)
        finish()
    }
}
