package com.capyreader.app

import android.os.Bundle
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.setThreadPolicy
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.capyreader.app.common.AppPreferences
import com.capyreader.app.ui.App
import com.capyreader.app.ui.Route
import org.koin.android.ext.android.get

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableStrictModeOnDebug()
        super.onCreate(savedInstanceState)

        setContent {
            App(
                startDestination = startDestination(),
            )
        }
    }

    private fun startDestination(): String {
        val appPreferences = get<AppPreferences>()

        val accountID = appPreferences.accountID.get()

        return if (accountID.isBlank()) {
            Route.AddAccount.path
        } else {
            Route.Articles.path
        }
    }
}

private fun enableStrictModeOnDebug() {
    if (BuildConfig.DEBUG) {
        setThreadPolicy(
            ThreadPolicy.Builder()
                .detectNetwork()
                .penaltyDeath()
                .build()
        )
    }
}
