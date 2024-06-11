package com.jocmp.basilreader.common

import android.content.Context
import android.util.Log
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.jocmp.basil.DatabaseProvider
import com.jocmp.basil.db.Database

class AndroidDatabaseProvider(private val context: Context) : DatabaseProvider {
    override fun build(accountID: String): Database {
        val driver = AndroidSqliteDriver(Database.Schema, context, databaseName(accountID))

        return Database(driver)
    }

    override fun delete(accountID: String) {
        val res = context.deleteDatabase(databaseName(accountID))
        Log.d("[DEBUG]", "delete: ${res}")
    }

    private fun databaseName(accountID: String): String {
        return "articles_$accountID"
    }
}
