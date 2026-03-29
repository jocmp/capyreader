package com.jocmp.capyreader.desktop

import com.jocmp.capy.Account
import com.jocmp.capy.AccountManager
import com.jocmp.capy.accounts.FaviconPolicy
import java.io.File

class AppState(private val dataDir: File) {
    private val accountsDir = File(dataDir, "accounts").apply { mkdirs() }
    private val cacheDir = File(dataDir, "cache").apply { mkdirs() }
    private val dbDir = File(dataDir, "db")
    private val prefsDir = File(dataDir, "prefs")
    private val accountIDFile = File(dataDir, "account_id")

    private val databaseProvider = FileDatabaseProvider(dbDir)
    private val preferenceStoreProvider = FilePreferenceStoreProvider(prefsDir)

    val manager = AccountManager(
        rootFolder = accountsDir.toURI(),
        cacheDirectory = cacheDir.toURI(),
        databaseProvider = databaseProvider,
        preferenceStoreProvider = preferenceStoreProvider,
        faviconPolicy = FaviconPolicy { true },
        userAgent = "CapyReaderDesktop/1.0",
        acceptLanguage = "en-US",
    )

    fun savedAccountID(): String? {
        if (!accountIDFile.exists()) return null
        val id = accountIDFile.readText().trim()
        return id.ifBlank { null }
    }

    fun saveAccountID(id: String) {
        accountIDFile.writeText(id)
    }

    fun clearAccountID() {
        accountIDFile.delete()
    }

    fun loadAccount(): Account? {
        val id = savedAccountID() ?: return null
        return manager.findByID(id)
    }
}
