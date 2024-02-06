package com.jocmp.basil

import kotlinx.coroutines.runBlocking
import okhttp3.OkHttpClient
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AccountManagerTest {
    @JvmField
    @Rule
    var rootFolder = TemporaryFolder()

    private fun buildManager(): AccountManager {
        return AccountManager(
            rootFolder = rootFolder.newFolder().toURI(),
            preferenceStoreProvider = InMemoryPreferencesProvider(),
            databaseProvider = InMemoryDatabaseProvider,
            httpClient = OkHttpClient(),
        )
    }

    @Test
    fun addAccount() {
        val manager = buildManager()

        assertNotNull(manager.createAccount())
        assertEquals(1, manager.accountSize())
    }

    @Test
    fun removeAccount() {
        val manager = buildManager()

        val account = manager.createAccount()
        manager.createAccount()

        manager.removeAccount(account.id)

        assertEquals(1, manager.accountSize())
    }

    @Test
    fun findById() = runBlocking {
        val manager = buildManager()

        val expectedAccount = manager.createAccount()

        val account = manager.findByID(expectedAccount.id)

        assertEquals(expectedAccount.id, account!!.id)
    }

    @Test
    fun findByIdMissingAccount() = runBlocking {
        val manager = buildManager()

        assertNull(manager.findByID("bogus"))
    }
}
