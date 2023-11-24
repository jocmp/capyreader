package com.jocmp.basil

import java.io.File
import java.io.FileFilter
import java.net.URI
import java.util.UUID

class AccountManager(rootFolder: URI) {
    var accounts: MutableList<Account> = mutableListOf()
        private set

    private val accountFolder = File(rootFolder.path, directoryName)

    init {
        accountFolder.listFiles()?.forEach { file ->
            accounts.add(Account(id = file.name, path = file.toURI()))
        }
    }

    fun findByID(id: String): Account {
        return accounts.find { it.id == id }!!
    }

    fun createAccount(): Account {
        val accountID = UUID.randomUUID().toString()

        if (!accountFolder.exists()) {
            accountFolder.mkdir()
        }

        val folder = accountFile(accountID).apply {
            mkdir()
        }

        return Account(id = accountID, path = folder.toURI()).also { account ->
            accounts.add(account)
        }
    }

    fun removeAccount(account: Account) {
        val files = accountFolder.listFiles(AccountFileFilter(account.id))

        val success = files?.first()?.deleteRecursively() ?: false

        if (success) {
            accounts.removeIf { it.id == account.id }
        }
    }

    private fun accountFile(id: String): File {
        return File(accountFolder, id)
    }

    companion object {
        private const val directoryName = "accounts"
    }
}

private class AccountFileFilter(private val id: String) : FileFilter {
    override fun accept(pathname: File?) = pathname?.name == id
}
