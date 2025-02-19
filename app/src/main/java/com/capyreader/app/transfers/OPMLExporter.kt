package com.capyreader.app.transfers

import android.content.Context
import android.net.Uri
import android.widget.Toast
import com.capyreader.app.R
import com.jocmp.capy.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
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

        Toast.makeText(context, context.getString(messageRes), Toast.LENGTH_SHORT).show()
    }

    companion object {
        val DEFAULT_FILE_NAME = "subscriptions.xml"
    }
}
