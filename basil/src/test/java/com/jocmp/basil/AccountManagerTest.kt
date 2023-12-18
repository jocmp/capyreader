package com.jocmp.basil

import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class AccountManagerTest {
    @JvmField
    @Rule
    var rootFolder = TemporaryFolder()

    private fun buildManager(): AccountManager {
        return AccountManager(
            rootFolder = rootFolder.newFolder().toURI(),
            databaseProvider = InMemoryDatabaseProvider()
        )
    }

    @Test
    fun addAccount() {
        val manager = buildManager()

        assertNotNull(manager.createAccount())
        assertEquals(1, manager.accounts.size)
    }

    @Test
    fun removeAccount() {
        val manager = buildManager()

        val account = manager.createAccount()
        manager.createAccount()

        manager.removeAccount(account)

        assertEquals(1, manager.accounts.size)
    }

    @Test
    fun findById() {
        val manager = buildManager()

        val expectedAccount = manager.createAccount()

        val account = manager.findByID(expectedAccount.id)

        assertEquals(expectedAccount, account)
    }

    @Test(expected = NullPointerException::class)
    fun findByIdMissingAccount() {
        val manager = buildManager()

        manager.findByID("bogus")
    }
}
