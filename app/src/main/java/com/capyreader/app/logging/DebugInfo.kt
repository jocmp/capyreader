package com.capyreader.app.logging

import android.os.Build
import com.capyreader.app.BuildConfig
import com.jocmp.capy.accounts.Source

fun buildDebugInfo(source: Source): String {
    return """
        App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.FLAVOR}, ${BuildConfig.VERSION_CODE})
        Android version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}; build ${Build.DISPLAY})
        Device brand: ${Build.BRAND}
        Device manufacturer: ${Build.MANUFACTURER}
        Device name: ${Build.DEVICE} (${Build.PRODUCT})
        Device model: ${Build.MODEL}
        Account source: ${source.name}
    """.trimIndent()
}
