package com.jocmp.capyreader.desktop

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.jocmp.capy.DatabaseProvider
import com.jocmp.capy.db.Database
import java.io.File

class FileDatabaseProvider(private val dbDir: File) : DatabaseProvider {
    init {
        dbDir.mkdirs()
    }

    override fun build(accountID: String): Database {
        val dbFile = File(dbDir, "articles_${accountID}.db")
        val isNew = !dbFile.exists()

        val driver = JdbcSqliteDriver("jdbc:sqlite:${dbFile.absolutePath}")

        if (isNew) {
            Database.Schema.create(driver)
        }

        driver.execute(null, "PRAGMA journal_mode = WAL", 0)
        driver.execute(null, "PRAGMA synchronous = NORMAL", 0)

        return Database(driver)
    }

    override fun delete(accountID: String) {
        File(dbDir, "articles_${accountID}.db").delete()
    }
}
