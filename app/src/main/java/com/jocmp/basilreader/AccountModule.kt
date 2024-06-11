package com.jocmp.basilreader

import com.jocmp.basil.Account
import com.jocmp.basil.AccountManager
import com.jocmp.basil.DatabaseProvider
import com.jocmp.basil.db.Database
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
