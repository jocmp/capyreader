package com.jocmp.basil.common

import app.cash.sqldelight.TransactionWithoutReturn
import com.jocmp.basil.db.Database

fun Database.transactionWithErrorHandling(
    body: TransactionWithoutReturn.() -> Unit
) {
    try {
        transaction(noEnclosing = false, body)
    } catch(e: Exception) {
        // continue
    }
}
