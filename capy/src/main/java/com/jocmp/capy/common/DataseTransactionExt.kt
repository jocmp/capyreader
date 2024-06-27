package com.jocmp.capy.common

import android.util.Log
import app.cash.sqldelight.TransactionWithoutReturn
import com.jocmp.capy.db.Database

fun Database.transactionWithErrorHandling(
    body: TransactionWithoutReturn.() -> Unit
) {
    try {
        transaction(noEnclosing = false, body)
    } catch(e: Exception) {
        // continue
    }
}
