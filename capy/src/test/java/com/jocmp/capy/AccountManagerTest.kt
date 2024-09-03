package com.jocmp.capy

import com.jocmp.capy.accounts.Source
import kotlinx.coroutines.runBlocking
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.io.File
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
            cacheDirectory = rootFolder.newFolder().toURI(),
            databaseProvider = InMemoryDatabaseProvider,
        )
    }

    @Test
    fun addAccount() {
        val manager = buildManager()

        assertNotNull(manager.createAccount("foo", "bar", Source.LOCAL))
    }

    @Test
    fun findById() = runBlocking {
        val manager = buildManager()

        val accountID = manager.createAccount("foo", "bar", Source.LOCAL)

        val account = manager.findByID(accountID)

        assertEquals(accountID, account!!.id)
    }

    @Test
    fun findByIdMissingAccount() = runBlocking {
        val manager = buildManager()

        assertNull(manager.findByID("bogus"))
    }
}
