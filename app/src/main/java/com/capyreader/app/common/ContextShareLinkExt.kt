package com.capyreader.app.common

import android.content.Context
import android.content.Intent
import android.net.Uri

fun Context.shareLink(url: String, title: String) {
    val share = Intent.createChooser(Intent().apply {
        type = "text/plain"
        action = Intent.ACTION_SEND
        putExtra(Intent.EXTRA_TEXT, url)
        putExtra(Intent.EXTRA_TITLE, title)
    }, null)
    startActivity(share)
}

fun Context.shareImage(uri: Uri) {
    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        putExtra(Intent.EXTRA_STREAM, uri)
        setDataAndType(uri, "image/jpeg")
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }
    startActivity(Intent.createChooser(shareIntent, null))
}
