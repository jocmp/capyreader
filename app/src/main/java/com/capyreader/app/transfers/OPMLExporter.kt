package com.capyreader.app.transfers

import android.content.Context
import android.net.Uri
import com.capyreader.app.R
import com.capyreader.app.common.toast
import com.jocmp.capy.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.FileOutputStream

class OPMLExporter(
    private val context: Context,
) {
    suspend fun export(account: Account, target: Uri?) {
        target ?: return

        val result = runCatching {
            withContext(Dispatchers.IO) {
                val source = account.opmlDocument().toByteArray()

                context.contentResolver.openFileDescriptor(target, "w")?.use { descriptor ->
                    FileOutputStream(descriptor.fileDescriptor).use {
                        it.write(source)
                    }
                }
            }
        }

        val messageRes = result.fold(
            onSuccess = { R.string.opml_exporter_success },
            onFailure = { R.string.opml_exporter_failure }
        )

        context.toast(messageRes)
    }

    companion object {
        val DEFAULT_FILE_NAME = "subscriptions.xml"
    }
}
