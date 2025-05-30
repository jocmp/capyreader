package com.capyreader.app.common

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.jocmp.capy.DatabaseProvider
import com.jocmp.capy.db.Database
import com.jocmp.capy.db.Sharing_services
import com.jocmp.capy.logging.CapyLog

class AndroidDatabaseProvider(private val context: Context) : DatabaseProvider {
    override fun build(accountID: String): Database {
        val driver = AndroidSqliteDriver(
            Database.Schema,
            context,
            databaseName(accountID),
            windowSizeBytes = 100 * 1024 * 1024,
            /**
             * - https://developer.android.com/topic/performance/sqlite-performance-best-practices#enable-write-ahead
             */
            callback = object : AndroidSqliteDriver.Callback(Database.Schema) {
                override fun onConfigure(db: SupportSQLiteDatabase) {
                    super.onConfigure(db)
                    tryEnableWriteAheadLogging(db)
                }

                private fun tryEnableWriteAheadLogging(db: SupportSQLiteDatabase) {
                    try {
                        db.enableWriteAheadLogging()
                        db.execSQL("PRAGMA synchronous = NORMAL")
                    } catch (e: Exception) {
                        CapyLog.error("androiddb", e)
                    }
                }
            }
        )

        return Database(
            driver,
            sharing_servicesAdapter = Sharing_services.Adapter(
                service_idAdapter = EnumColumnAdapter()
            )
        )
    }

    override fun delete(accountID: String) {
        context.deleteDatabase(databaseName(accountID))
    }

    private fun databaseName(accountID: String): String {
        return "articles_$accountID"
    }
}
