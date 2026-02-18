package com.capyreader.app

import android.content.Context
import android.webkit.WebSettings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.capyreader.app.common.AndroidClientCertManager
import com.capyreader.app.common.AndroidDatabaseProvider
import com.capyreader.app.common.AppFaviconPolicy
import com.capyreader.app.common.DataStorePreferenceStoreProvider
import com.capyreader.app.preferences.AppPreferences
import com.jocmp.capy.AccountManager
import com.jocmp.capy.ClientCertManager
import com.jocmp.capy.DatabaseProvider
import com.jocmp.capy.PreferenceStoreProvider
import com.jocmp.capy.preferences.AndroidPreferenceStore
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module
import java.util.Locale

private val Context.appDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "app_preferences",
    produceMigrations = { context ->
        listOf(SharedPreferencesMigration(context, "${context.packageName}_preferences"))
    }
)

internal val common = module {
    single<PreferenceStoreProvider> { DataStorePreferenceStoreProvider(get()) }
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
            acceptLanguage = Locale.getDefault().toAcceptLanguageTag(),
        )
    }
    single { AppPreferences(AndroidPreferenceStore(androidContext().appDataStore)) }
}

private fun Locale.toAcceptLanguageTag(): String {
    val primary = toLanguageTag()
    val language = language
    return if (primary != language) {
        "$primary,$language;q=0.9"
    } else {
        primary
    }
}
