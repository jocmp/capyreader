package com.jocmp.basilreader

import android.content.Context
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.jocmp.basil.DatabaseProvider
import com.jocmp.basil.db.Database

class AndroidDatabaseProvider(private val context: Context) : DatabaseProvider {
    override fun forAccount(accountID: String): Database {
        val driver = AndroidSqliteDriver(Database.Schema, context, "articles_$accountID")

        return Database(driver)
    }
}
