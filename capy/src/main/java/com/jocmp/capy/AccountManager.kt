package com.jocmp.capy

import com.jocmp.capy.db.Database
import java.io.File
import java.io.FileFilter
import java.net.URI
import java.util.UUID

class AccountManager(
    val rootFolder: URI,
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

    fun createAccount(username: String, password: String): String {
        val accountID = UUID.randomUUID().toString()

        accountFolder().apply {
            if (!exists()) {
                mkdir()
            }
        }

        accountFile(accountID).apply { mkdir() }

        val preferences = preferenceStoreProvider.build(accountID)
        preferences.username.set(username)
        preferences.password.set(password)

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
            database = database,
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
