package com.jocmp.capyreader.common

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

fun Context.fileURI(file: File): Uri =
    FileProvider.getUriForFile(this, "${packageName}.fileprovider", file)
