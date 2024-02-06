package com.jocmp.basilreader

import android.os.Bundle
import android.os.StrictMode.ThreadPolicy
import android.os.StrictMode.setThreadPolicy
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.jocmp.basil.AccountManager
import com.jocmp.basilreader.common.AppPreferences
import com.jocmp.basilreader.ui.App
import org.koin.android.ext.android.get

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableStrictMode()
        super.onCreate(savedInstanceState)

        setContent {
            App(startDestination())
        }
    }

    private fun startDestination(): String {
        val accountManager = get<AccountManager>()
        val appPreferences = get<AppPreferences>()

        val account = accountManager.findByID(appPreferences.accountID.get())

        return if (account == null) {
            "accounts"
        } else {
            "articles"
        }
    }
}

private fun enableStrictMode() {
    if (BuildConfig.DEBUG) {
        setThreadPolicy(
            ThreadPolicy.Builder()
                .detectNetwork()
                .penaltyDeath()
                .build()
        )
    }
}
