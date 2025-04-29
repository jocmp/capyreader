package com.capyreader.app.sync

import com.jocmp.capy.logging.CapyLog

internal object SyncLogger {
    fun logError(
        error: Throwable,
        value: Boolean,
        workType: String,
        articleIDs: List<String>
    ) {
        CapyLog.error(
            "sync_work",
            error,
            mapOf(
                "work_type" to workType,
                "work_value" to value.toString(),
                "size" to articleIDs.size.toString()
            )
        )
    }
}
