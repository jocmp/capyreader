package com.capyreader.app.common

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.capyreader.app.BuildConfig
import java.io.File

fun Context.fileURI(file: File): Uri =
    FileProvider.getUriForFile(this, "${BuildConfig.APPLICATION_ID}.fileprovider", file)

fun Context.externalImageCacheFile(name: String): File {
    val imageCache = File(externalCacheDir, "images").apply {
        if (!exists()) {
            mkdir()
        }
    }

    return File(imageCache, name).apply {
        createNewFile()
    }
}

fun Context.createCacheFile(name: String): File {
    return File(externalCacheDir, name).apply {
       if (exists()) {
           delete()
       }
        createNewFile()
    }
}
