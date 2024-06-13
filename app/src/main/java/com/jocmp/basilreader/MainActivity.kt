package com.jocmp.basilreader

import android.os.Bundle
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.setThreadPolicy
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import com.jocmp.basilreader.common.AppPreferences
import com.jocmp.basilreader.ui.App
import com.jocmp.basilreader.ui.Route
import org.koin.android.ext.android.get

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        enableStrictModeOnDebug()
        super.onCreate(savedInstanceState)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)

            App(
                startDestination = startDestination(),
                windowSizeClass = windowSizeClass
            )
        }
    }

    private fun startDestination(): String {
        val appPreferences = get<AppPreferences>()

        val accountID = appPreferences.accountID.get()

        return if (accountID.isBlank()) {
            Route.Login.path
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
