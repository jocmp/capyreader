package com.capyreader.lite

import android.app.Application
import com.capyreader.lite.preferences.LitePreferences
import com.google.android.material.color.DynamicColors
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class LiteApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)

        startKoin {
            androidContext(this@LiteApplication)
            modules(coreModule)
        }

        if (get<LitePreferences>().isLoggedIn) {
            loadLiteAccountModules()
        }
    }
}
