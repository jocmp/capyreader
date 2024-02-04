package com.jocmp.basilreader

import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import com.jocmp.basil.AccountManager
import com.jocmp.basilreader.ui.App
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.get
import org.koin.android.ext.android.inject
import org.koin.compose.koinInject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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
