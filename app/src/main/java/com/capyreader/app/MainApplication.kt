package com.capyreader.app

import android.app.Application
import com.capyreader.app.common.AppPreferences
import com.google.android.material.color.DynamicColors
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)

        startKoin {
            androidContext(this@MainApplication)
            workManagerFactory()
            setupCommonModules()
        }

        if (get<AppPreferences>().accountID.get().isNotBlank()) {
            loadAccountModules()
        }
    }
}
