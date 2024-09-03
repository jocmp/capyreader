package com.jocmp.capy

import com.jocmp.capy.accounts.Source
import com.jocmp.capy.db.Database
import java.io.File
import java.io.FileFilter
import java.net.URI
import java.util.UUID

class AccountManager(
    val rootFolder: URI,
    private val cacheDirectory: File,
    private val databaseProvider: DatabaseProvider,
    private val preferenceStoreProvider: PreferenceStoreProvider,
) {
    fun findByID(
        id: String,
        database: Database = databaseProvider.build(id)
    ): Account? {
        val existingAccount = findAccountFile(id) ?: return null

        return buildAccount(existingAccount, database)
    }

    fun createAccount(username: String, password: String, source: Source): String {
        val accountID = createAccount(source = source)

        preferenceStoreProvider.build(accountID).let { preferences ->
            preferences.username.set(username)
            preferences.password.set(password)
        }

        return accountID
    }

    fun createAccount(source: Source): String {
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

    private fun buildAccount(
        path: File,
        database: Database,
        preferences: AccountPreferences = preferenceStoreProvider.build(path.name)
    ): Account {
        val id = path.name
        val pathURI = path.toURI()

        return Account(
            id = id,
            path = pathURI,
            cacheDirectory = cacheDirectory,
            database = database,
            source = preferences.source.get(),
            preferences = preferences,
        )
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
