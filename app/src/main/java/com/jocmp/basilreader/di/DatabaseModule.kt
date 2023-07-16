package com.jocmp.basilreader.di

import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.jocmp.feedbinclient.db.FeedbinDatabase
import org.koin.dsl.module

internal val databaseModule = module {
    single {
        FeedbinDatabase(
            driver = AndroidSqliteDriver(
                schema = FeedbinDatabase.Schema,
                context = get(),
                name = "feedbin.db"
            )
        )
    }
}
