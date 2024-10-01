package com.capyreader.app.ui

import com.capyreader.app.BuildConfig

object CrashReporting {
    const val isAvailable = BuildConfig.FLAVOR == "gplay"
}
