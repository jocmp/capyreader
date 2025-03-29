package com.capyreader.app.common

import android.content.Context
import android.content.Intent

fun Context.shareLink(url: String, title: String) {
    val share = Intent.createChooser(Intent().apply {
        type = "text/plain"
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, url)
        putExtra(Intent.EXTRA_TITLE, title)
    }, null)
    startActivity(share)
}
