package com.capyreader.app

import com.jocmp.capy.Account
import com.jocmp.capy.AccountManager
import com.jocmp.capy.DatabaseProvider
import com.jocmp.capy.db.Database
import org.koin.core.parameter.parametersOf
import org.koin.dsl.module

val accountModule = module {
    single<Database> { params ->
        get<DatabaseProvider>().build(accountID = params.get())
    }
    single<Account> { params ->
        val database = get<Database>(parameters = { parametersOf(params.get()) })
        get<AccountManager>().findByID(id = params.get(), database = database)!!
    }
}
