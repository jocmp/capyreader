package com.jocmp.basilreader

import android.app.Application
import com.jocmp.basilreader.di.setupModules
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class MainApplication: Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@MainApplication)
            setupModules()
        }
    }

}
