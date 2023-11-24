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

    @Test
    fun addAccount() {
        val manager = AccountManager(rootFolder.newFolder().toURI())

        assertNotNull(manager.createAccount())
        assertEquals(1, manager.accounts.size)
    }

    @Test
    fun removeAccount() {
        val manager = AccountManager(rootFolder.newFolder().toURI())

        val account = manager.createAccount()
        manager.createAccount()

        manager.removeAccount(account)

        assertEquals(1, manager.accounts.size)
    }

    @Test
    fun findById() {
        val manager = AccountManager(rootFolder.newFolder().toURI())

        val expectedAccount = manager.createAccount()

        val account = manager.findByID(expectedAccount.id)

        assertEquals(expectedAccount, account)
    }

    @Test(expected = NullPointerException::class)
    fun findByIdMissingAccount() {
        val manager = AccountManager(rootFolder.newFolder().toURI())

        manager.findByID("bogus")
    }
}
