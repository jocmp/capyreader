package com.capyreader.app.transfers

import android.content.Context
import android.content.Intent
import com.jocmp.capy.Account
import com.capyreader.app.R
import com.capyreader.app.common.fileURI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption

class OPMLExporter(
    private val context: Context,
) {
    suspend fun export(account: Account) = withContext(Dispatchers.IO) {
        val exports = File(context.filesDir, "transfers")
        exports.mkdirs()
        val source = File(exports, "source.xml").apply {
            writeText(account.opmlDocument())
        }
        val export = File(exports, "subscriptions.xml")
        val target = export.toPath()

        if (!source.exists()) {
            return@withContext
        }

        val result = Files.copy(source.toPath(), target, StandardCopyOption.REPLACE_EXISTING)

        source.delete()

        withContext(Dispatchers.Main) {
            try {
                val uri = context.fileURI(result.toFile())
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/xml"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(
                    Intent.createChooser(
                        shareIntent,
                        context.getString(R.string.transfers_export_subscriptions)
                    )
                )
            } catch (e: IllegalArgumentException) {
                // no-op
            }
        }
    }
}
