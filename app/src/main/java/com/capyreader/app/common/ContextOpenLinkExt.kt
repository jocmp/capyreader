package com.capyreader.app.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import java.net.URL

fun Context.openLink(url: Uri, appPreferences: AppPreferences? = null) {
    if (appPreferences != null && appPreferences.openLinksInternally.get()) {
        val intent = CustomTabsIntent
            .Builder()
            .build()

        intent.launchUrl(this, url)
    } else {
        openLinkExternally(url)
    }
}

fun Context.openLinkExternally(url: URL) {
    openLinkExternally(Uri.parse(url.toString()))
}

private fun Context.openLinkExternally(url: Uri) {
    val intent = Intent(Intent.ACTION_VIEW, url)
    startActivity(intent)
}
