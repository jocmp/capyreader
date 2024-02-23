package com.jocmp.basil

import okhttp3.OkHttpClient
import java.io.File
import java.io.FileFilter
import java.net.URI
import java.util.UUID

class AccountManager(
    val rootFolder: URI,
    private val databaseProvider: DatabaseProvider,
    private val preferenceStoreProvider: PreferenceStoreProvider,
) {
    fun findByID(id: String?): Account? {
        id ?: return null

        val existingAccount = findAccountFile(id) ?: return null

        return buildAccount(existingAccount)
    }

    val accounts: List<Account>
        get() = listAccounts().map { buildAccount(it) }

    fun accountSize(): Int {
        return listAccounts().size
    }

    fun createAccount(username: String, password: String): Account {
        val accountID = UUID.randomUUID().toString()

        accountFolder().apply {
            if (!exists()) {
                mkdir()
            }
        }

        val file = accountFile(accountID).apply { mkdir() }

        val preferences = preferenceStoreProvider.build(accountID)
        preferences.username.set(username)
        preferences.password.set(password)

        return buildAccount(file, preferences)
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

    private fun listAccounts(): List<File> {
        return accountFolder().listFiles()?.toList() ?: emptyList()
    }

    private fun accountFolder() = File(rootFolder.path, directoryName)

    private fun buildAccount(
        path: File,
        preferences: AccountPreferences = preferenceStoreProvider.build(path.name)
    ): Account {
        val id = path.name
        val pathURI = path.toURI()

        return Account(
            id = id,
            path = pathURI,
            database = databaseProvider.build(id),
            preferences = preferences,
        )
    }

    private fun findAccountFile(id: String): File? {
        return accountFolder().listFiles(AccountFileFilter(id))?.firstOrNull()
    }

    companion object {
        private const val directoryName = "accounts"
    }
}

private class AccountFileFilter(private val id: String) : FileFilter {
    override fun accept(pathname: File?) = pathname?.name == id
}
