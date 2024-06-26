package com.jocmp.capy

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.jocmp.capy.db.Database

object InMemoryDatabaseProvider: DatabaseProvider {
    override fun build(accountID: String): Database {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        Database.Schema.create(driver)
        return Database(driver)
    }

    override fun delete(accountID: String) {
    }
}
