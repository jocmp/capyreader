package com.jocmp.basilreader.ui.accounts

import android.content.Context
import android.content.Intent
import com.jocmp.basil.Account
import com.jocmp.basilreader.R
import com.jocmp.basilreader.common.fileURI
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.io.path.exists

class OPMLExporter(
    private val context: Context,
) {
    fun export(account: Account) {
        val exports = File(context.filesDir, "transfers")
        exports.mkdirs()
        val export = File(exports, "${account.displayName}.xml")

        val source = File(account.opmlFile.path).toPath()
        val target = export.toPath()

        if (!source.exists()) {
            return
        }

        val result = Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING)

        return try {
            val uri = context.fileURI(result.toFile())
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/xml"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(
                Intent.createChooser(
                    shareIntent,
                    context.getString(R.string.opml_exporter_chooser_title, account.displayName)
                )
            )
        } catch (e: IllegalArgumentException) {
            // return
        }
    }
}
