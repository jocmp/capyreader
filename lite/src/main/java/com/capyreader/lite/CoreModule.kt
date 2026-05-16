package com.capyreader.lite

import android.webkit.WebSettings
import com.capyreader.lite.common.AndroidClientCertManager
import com.capyreader.lite.common.AndroidDatabaseProvider
import com.capyreader.lite.common.AppFaviconPolicy
import com.capyreader.lite.common.SharedPreferenceStoreProvider
import com.capyreader.lite.preferences.LitePreferences
import com.capyreader.lite.ui.feeds.FraidycatViewModel
import com.capyreader.lite.ui.login.LiteLoginViewModel
import com.jocmp.capy.Account
import com.jocmp.capy.AccountManager
import com.jocmp.capy.ClientCertManager
import com.jocmp.capy.DatabaseProvider
import com.jocmp.capy.PreferenceStoreProvider
import com.jocmp.capy.accounts.httpClientBuilder
import com.jocmp.capy.db.Database
import okhttp3.OkHttpClient
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module
import java.util.Locale

internal val coreModule = module {
    single<OkHttpClient> {
        httpClientBuilder(cachePath = androidContext().cacheDir.toURI()).build()
    }
    single<PreferenceStoreProvider> { SharedPreferenceStoreProvider(get()) }
    single<DatabaseProvider> { AndroidDatabaseProvider(context = get()) }
    single<ClientCertManager> { AndroidClientCertManager(context = get()) }
    single {
        AccountManager(
            rootFolder = androidContext().filesDir.toURI(),
            databaseProvider = get(),
            cacheDirectory = androidContext().cacheDir.toURI(),
            preferenceStoreProvider = get(),
            faviconPolicy = AppFaviconPolicy(get()),
            clientCertManager = get(),
            userAgent = WebSettings.getDefaultUserAgent(androidContext()),
            acceptLanguage = Locale.getDefault().toLanguageTag(),
        )
    }
    single { LitePreferences(get()) }
    viewModel { LiteLoginViewModel(accountManager = get(), litePreferences = get()) }
}

internal val accountModule = module {
    single<Database> {
        get<DatabaseProvider>().build(accountID = get<LitePreferences>().accountID.get())
    }
    single<Account> {
        get<AccountManager>().findByID(
            id = get<LitePreferences>().accountID.get(),
            database = get<Database>()
        )!!
    }
    single { FraidycatViewModel(account = get()) }
}

fun loadLiteAccountModules() {
    loadKoinModules(accountModule)
}

fun unloadLiteAccountModules() {
    unloadKoinModules(accountModule)
}
