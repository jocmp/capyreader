package com.jocmp.capy.common

import app.cash.sqldelight.TransactionWithoutReturn
import com.jocmp.capy.db.Database
import com.jocmp.capy.logging.CapyLog

fun Database.transactionWithErrorHandling(
    body: TransactionWithoutReturn.() -> Unit
) {
    try {
        transaction(noEnclosing = false, body)
    } catch(e: Throwable) {
        CapyLog.error("db_error", e)
    }
}
