package com.capyreader.app

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.capyreader.app.common.toast
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.ThemePreference
import com.capyreader.app.ui.addintent.AddLinkScreen
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.Account
import org.koin.android.ext.android.inject

class AddLinkActivity : BaseActivity() {
    val appPreferences by inject<AppPreferences>()
    val account by inject<Account>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val defaultQueryURL = intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty()
        val pageTitle = intent.getStringExtra(Intent.EXTRA_SUBJECT).orEmpty()

        setContent {
            var initialTheme by remember { mutableStateOf<ThemePreference?>(null) }

            LaunchedEffect(Unit) {
                if (appPreferences.accountID.get().isBlank()) {
                    toast(R.string.widget_headlines_account_error)
                    finish()
                    return@LaunchedEffect
                }
                initialTheme = appPreferences.themePreference()
            }

            val theme = initialTheme

            if (theme != null) {
                CapyTheme(appPreferences, initialTheme = theme) {
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
    }

    override fun onStop() {
        super.onStop()
        finish()
    }
}
