package com.capyreader.app.common

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.capyreader.app.BuildConfig
import java.io.File

fun Context.fileURI(file: File): Uri =
    FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", file)

fun Context.createCacheFile(name: String): File {
    val file = File(externalCacheDir, name)
    if (file.exists()) {
        file.delete()
    }
    file.createNewFile()
    return file
}
