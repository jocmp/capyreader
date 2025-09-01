package com.capyreader.app.common

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import com.jocmp.capy.logging.CapyLog

fun openLink(
    context: Context,
    url: Uri,
    openExternalAdjacent: Boolean,
    openInternally: Boolean,
) {
    try {
        if (openInternally) {
            val intent = CustomTabsIntent
                .Builder()
                .build()

            intent.launchUrl(context, url)
        } else {
            context.openLinkExternally(url, openAdjacent = openExternalAdjacent)
        }
    } catch (e: Throwable) {
        CapyLog.error("open_link", e)
    }
}

private fun Context.openLinkExternally(url: Uri, openAdjacent: Boolean) {
    Intent(Intent.ACTION_VIEW)
        .apply {
            data = url

            if (openAdjacent) {
                addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or Intent.FLAG_ACTIVITY_NEW_TASK)
            } else {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }.also { intent ->
            startActivity(intent)
        }
}
