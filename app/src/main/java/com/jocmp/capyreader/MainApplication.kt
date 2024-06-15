package com.jocmp.capyreader

import android.app.Application
import com.jocmp.capyreader.common.AppPreferences
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()

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
