package com.jocmp.capy

import com.jocmp.capy.accounts.FaviconPolicy
import com.jocmp.capy.accounts.LocalOkHttpClient
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.accounts.feedbin.FeedbinAccountDelegate
import com.jocmp.capy.accounts.feedbin.FeedbinOkHttpClient
import com.jocmp.capy.accounts.forAccount
import com.jocmp.capy.accounts.local.LocalAccountDelegate
import com.jocmp.capy.accounts.miniflux.MinifluxAccountDelegate
import com.jocmp.capy.accounts.reader.buildReaderDelegate
import com.jocmp.capy.db.Database
import com.jocmp.feedbinclient.Feedbin
import com.jocmp.minifluxclient.Miniflux
import java.io.File
import java.io.FileFilter
import java.net.URI
import java.util.UUID

class AccountManager(
    val rootFolder: URI,
    private val cacheDirectory: URI,
    private val databaseProvider: DatabaseProvider,
    private val preferenceStoreProvider: PreferenceStoreProvider,
    private val faviconPolicy: FaviconPolicy,
    private val clientCertManager: ClientCertManager,
    private val userAgent: String,
    private val acceptLanguage: String,
) {
    suspend fun findByID(
        id: String,
        database: Database = databaseProvider.build(id),
    ): Account? {
        val existingAccount = findAccountFile(id) ?: return null

        return buildAccount(existingAccount, database)
    }

    suspend fun createAccount(
        username: String,
        password: String,
        url: String,
        clientCertAlias: String,
        source: Source
    ): String {
        val accountID = createAccount(source = source)

        preferenceStoreProvider.build(accountID).let { preferences ->
            preferences.username.set(username)
            preferences.password.set(password)
            preferences.url.set(url)
            preferences.clientCertAlias.set(clientCertAlias)
        }

        return accountID
    }

    suspend fun createAccount(source: Source): String {
        val accountID = UUID.randomUUID().toString()

        accountFolder().apply {
            if (!exists()) {
                mkdir()
            }
        }

        preferenceStoreProvider.build(accountID).source.set(source)

        accountFile(accountID).apply { mkdir() }

        return accountID
    }

    fun removeAccount(accountID: String) {
        val accountFile = findAccountFile(accountID)

        accountFile?.deleteRecursively()
        databaseProvider.delete(accountID)
        preferenceStoreProvider.delete(accountID)
    }

    private fun accountFile(id: String): File {
        return File(accountFolder(), id)
    }

    private fun accountFolder() = File(rootFolder.path, DIRECTORY_NAME)

    private suspend fun buildAccount(
        path: File,
        database: Database,
        preferences: AccountPreferences = preferenceStoreProvider.build(path.name)
    ): Account {
        val id = path.name
        val pathURI = path.toURI()
        val cacheDirectory = File(cacheDirectory.path, id).toURI()
        val source = preferences.source.get()

        val delegate = buildDelegate(
            source = source,
            database = database,
            cacheDirectory = cacheDirectory,
            preferences = preferences,
        )

        return Account(
            id = id,
            path = pathURI,
            cacheDirectory = cacheDirectory,
            database = database,
            source = source,
            preferences = preferences,
            faviconPolicy = faviconPolicy,
            clientCertManager = clientCertManager,
            userAgent = userAgent,
            acceptLanguage = acceptLanguage,
            delegate = delegate,
        )
    }

    private suspend fun buildDelegate(
        source: Source,
        database: Database,
        cacheDirectory: URI,
        preferences: AccountPreferences,
    ): AccountDelegate {
        return when (source) {
            Source.LOCAL -> LocalAccountDelegate(
                database = database,
                httpClient = LocalOkHttpClient.forAccount(path = cacheDirectory),
                preferences = preferences,
            )

            Source.FEEDBIN -> {
                val username = preferences.username.get()
                val password = preferences.password.get()

                FeedbinAccountDelegate(
                    database = database,
                    feedbin = Feedbin.create(
                        client = FeedbinOkHttpClient.forAccount(cacheDirectory, username, password)
                    )
                )
            }

            Source.MINIFLUX,
            Source.MINIFLUX_TOKEN -> {
                val baseURL = preferences.url.get()
                val username = preferences.username.get()
                val password = preferences.password.get()

                MinifluxAccountDelegate(
                    database = database,
                    miniflux = Miniflux.forAccount(
                        path = cacheDirectory,
                        baseURL = baseURL,
                        username = username,
                        password = password,
                        source = source,
                    ),
                    preferences = preferences,
                )
            }

            Source.FRESHRSS,
            Source.READER -> {
                val baseURL = preferences.url.get()
                val password = preferences.password.get()
                val clientCertAlias = preferences.clientCertAlias.get()

                buildReaderDelegate(
                    source = source,
                    database = database,
                    path = cacheDirectory,
                    baseURL = baseURL,
                    password = password,
                    clientCertAlias = clientCertAlias,
                    clientCertManager = clientCertManager,
                )
            }
        }
    }

    private fun findAccountFile(id: String): File? {
        return accountFolder().listFiles(AccountFileFilter(id))?.firstOrNull()
    }

    companion object {
        private const val DIRECTORY_NAME = "accounts"
    }
}

private class AccountFileFilter(private val id: String) : FileFilter {
    override fun accept(pathname: File?) = pathname?.name == id
}
