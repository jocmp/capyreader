package com.capyreader.common

import android.content.Context
import android.util.Log
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.jocmp.capy.DatabaseProvider
import com.jocmp.capy.db.Database

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
