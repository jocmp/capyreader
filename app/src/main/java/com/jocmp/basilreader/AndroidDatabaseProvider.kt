package com.jocmp.basilreader

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.jocmp.basil.DatabaseProvider
import com.jocmp.basil.db.Database

class AndroidDatabaseProvider(private val context: Context) : DatabaseProvider {
    override fun build(accountID: String): Database {
        val driver = AndroidSqliteDriver(Database.Schema, context, databaseName(accountID))

        return Database(driver)
    }

    override fun delete(accountID: String) {
        context.deleteDatabase(databaseName(accountID))
    }

    private fun databaseName(accountID: String): String {
        return "articles_$accountID"
    }
}
