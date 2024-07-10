package com.capyreader.app.common

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContract
import androidx.annotation.CallSuper

open class GetOPMLContent : ActivityResultContract<Unit, Uri?>() {
    @CallSuper
    override fun createIntent(context: Context, input: Unit): Intent {
        return Intent(Intent.ACTION_GET_CONTENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType("text/xml,text/x-opml,application/*")
            .putExtra(Intent.EXTRA_MIME_TYPES, listOf("text/xml", "text/x-opml", "application/*").toTypedArray())
    }

    final override fun getSynchronousResult(
        context: Context,
        input: Unit
    ): SynchronousResult<Uri?>? = null

    final override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
        return intent.takeIf { resultCode == Activity.RESULT_OK }?.data
    }
}
