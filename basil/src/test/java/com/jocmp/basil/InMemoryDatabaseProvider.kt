package com.jocmp.basil

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.jocmp.basil.db.Database
import java.net.URI

class InMemoryDatabaseProvider: DatabaseProvider {
    override fun forAccount(accountID: String): Database {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        Database.Schema.create(driver)
        return Database(driver)
    }
}
