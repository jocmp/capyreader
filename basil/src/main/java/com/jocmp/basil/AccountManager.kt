package com.jocmp.basil

import com.jocmp.basil.accounts.LocalAccountDelegate
import kotlinx.coroutines.flow.first
import java.io.File
import java.io.FileFilter
import java.net.URI
import java.util.UUID

class AccountManager(
    val rootFolder: URI,
    private val databaseProvider: DatabaseProvider,
    private val preferencesProvider: PreferencesProvider,
) {
    suspend fun findByID(id: String?): Account? {
        id ?: return null

        val existingAccount = listAccounts().find { file -> file.name == id } ?: return null

        val preferences = preferencesProvider.forAccount(existingAccount.name)
        val source = preferences.data.first().source

        return buildAccount(id = existingAccount.name, path = existingAccount)
    }

    suspend fun latestSummaries(): List<AccountSummary> {
        val ids = listAccounts().map { it.name }
        return ids.map { id ->
            val preferences = preferencesProvider
                .forAccount(id)
                .data
                .first()

            AccountSummary(id = id, preferences)
        }
    }

    suspend fun accountIDs(): List<String> {
        return latestSummaries().map { it.id }
    }

    fun isEmpty(): Boolean {
        return accountSize() == 0
    }

    fun accountSize(): Int {
        return listAccounts().size
    }

    fun createAccount(): AccountSummary {
        val accountID = UUID.randomUUID().toString()

        accountFolder().apply {
            if (!exists()) {
                mkdir()
            }
        }

        accountFile(accountID).mkdir()

        return AccountSummary(id = accountID,
            AccountPreferences(
                displayName = "Feedbin",
                source = AccountSource.LOCAL.value
            )
        )
    }

    fun removeAccount(accountID: String) {
        val files = accountFolder().listFiles(AccountFileFilter(accountID))

        files?.first()?.deleteRecursively() ?: false
    }

    private fun accountFile(id: String): File {
        return File(accountFolder(), id)
    }

    private fun listAccounts(): List<File> {
        return accountFolder().listFiles()?.toList() ?: emptyList()
    }

    private fun accountFolder() = File(rootFolder.path, directoryName)

    private fun buildAccount(id: String, path: File): Account {
        val pathURI = path.toURI()

        return Account(
            id = id,
            path = pathURI,
            database = databaseProvider.forAccount(id),
        )
    }

    class AccountSummary(
        val id: String,
        preferences: AccountPreferences,
    ) {
        val displayName = preferences.displayName
    }

    companion object {
        private const val directoryName = "accounts"
    }
}

private class AccountFileFilter(private val id: String) : FileFilter {
    override fun accept(pathname: File?) = pathname?.name == id
}
