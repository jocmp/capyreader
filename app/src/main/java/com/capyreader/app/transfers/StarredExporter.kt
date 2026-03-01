package com.capyreader.app.transfers

import android.content.Context
import android.net.Uri
import com.capyreader.app.R
import com.capyreader.app.common.toast
import com.jocmp.capy.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream

class StarredExporter(
    private val context: Context,
) {
    suspend fun export(account: Account, target: Uri?) {
        target ?: return

        val result = runCatching {
            withContext(Dispatchers.IO) {
                val source = account.starredBookmarksDocument().toByteArray()

                context.contentResolver.openFileDescriptor(target, "w")?.use { descriptor ->
                    FileOutputStream(descriptor.fileDescriptor).use {
                        it.write(source)
                    }
                }
            }
        }

        val messageRes = result.fold(
            onSuccess = { R.string.starred_exporter_success },
            onFailure = { R.string.starred_exporter_failure }
        )

        context.toast(messageRes)
    }

    companion object {
        const val DEFAULT_FILE_NAME = "starred.html"
    }
}
