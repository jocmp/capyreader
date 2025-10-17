package com.capyreader.app

import android.os.Bundle
import android.os.StrictMode
import android.os.StrictMode.setThreadPolicy
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.capyreader.app.ui.EdgeToEdgeHelper.isEdgeToEdgeAvailable

abstract class BaseActivity: ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableStrictModeOnDebug()

        if (isEdgeToEdgeAvailable()) {
            enableEdgeToEdge()
        }
    }
}

private fun enableStrictModeOnDebug() {
    if (BuildConfig.DEBUG) {
        setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectNetwork()
                .penaltyDeath()
                .build()
        )
    }
}
