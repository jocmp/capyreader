package com.capyreader.app.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.logging.CapyLog

fun Context.openLink(url: Uri, appPreferences: AppPreferences? = null) {
    try {
        if (appPreferences != null && appPreferences.openLinksInternally.get()) {
            val intent = CustomTabsIntent
                .Builder()
                .build()

            intent.launchUrl(this, url)
        } else {
            openLinkExternally(url)
        }
    } catch (e: Throwable) {
        CapyLog.error("open_link", e)
    }
}

fun Context.openLinkExternally(url: Uri) {
    Intent(Intent.ACTION_VIEW)
        .apply {
            data = url
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }.also { intent ->
            startActivity(intent)
        }
}