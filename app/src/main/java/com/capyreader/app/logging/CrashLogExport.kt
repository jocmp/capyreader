/**
 * Copyright © 2015 Javier Tomás
 * Copyright © 2024 The Mihon Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * https://github.com/mihonapp/mihon/blob/a3d438e2f5b427eb8b4c391ab9fe10c5a83baf29/app/src/main/java/eu/kanade/tachiyomi/util/CrashLogUtil.kt
 */
package com.capyreader.app.logging

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.capyreader.app.BuildConfig
import com.capyreader.app.R
import com.capyreader.app.common.createCacheFile
import com.capyreader.app.common.fileURI
import com.capyreader.app.common.toast
import com.jocmp.capy.common.withNonCancellableContext
import com.jocmp.capy.common.withUIContext

class CrashLogExport(private val context: Context) {
    suspend fun export(exception: Throwable? = null) = withNonCancellableContext {
        try {
            val file = context.createCacheFile("capy_crash_logs.txt")

            file.appendText(buildDebugInfo() + "\n\n")
            exception?.let { file.appendText("$it\n\n") }

            Runtime.getRuntime().exec("logcat *:E -d -f ${file.absolutePath}").waitFor()

            val uri = context.fileURI(file)
            context.startActivity(uri.toShareIntent(context, "text/plain"))
        } catch (e: Throwable) {
            withUIContext { context.toast("Failed to build logs") }
        }
    }

    private fun buildDebugInfo(): String {
        return """
            App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.FLAVOR}, ${BuildConfig.VERSION_CODE})
            Android version: ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT}; build ${Build.DISPLAY})
            Device brand: ${Build.BRAND}
            Device manufacturer: ${Build.MANUFACTURER}
            Device name: ${Build.DEVICE} (${Build.PRODUCT})
            Device model: ${Build.MODEL}
        """.trimIndent()
    }
}

private fun Uri.toShareIntent(context: Context, type: String): Intent {
    val uri = this

    val shareIntent = Intent(Intent.ACTION_SEND).apply {
        when (uri.scheme) {
            "content" -> {
                putExtra(Intent.EXTRA_STREAM, uri)
            }
        }
        clipData = ClipData.newRawUri(null, uri)
        setType(type)
        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
    }

    return Intent.createChooser(shareIntent, context.getString(R.string.action_share)).apply {
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }
}
