package com.capyreader.lite

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.capyreader.lite.preferences.LitePreferences
import com.capyreader.lite.theme.CapyLiteTheme
import com.capyreader.lite.ui.LiteApp
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    private val preferences by inject<LitePreferences>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CapyLiteTheme {
                LiteApp(startLoggedIn = preferences.isLoggedIn)
            }
        }
    }
}
