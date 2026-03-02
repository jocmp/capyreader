package com.jocmp.capy

import com.jocmp.capy.accounts.FakeFaviconPolicy
import com.jocmp.capy.accounts.Source
import kotlinx.coroutines.test.runTest
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
            cacheDirectory = rootFolder.newFolder().toURI(),
            databaseProvider = InMemoryDatabaseProvider,
            faviconPolicy = FakeFaviconPolicy,
            clientCertManager = FakeClientCertManager,
            userAgent = "TestUserAgent",
            acceptLanguage = "en-US",
        )
    }

    @Test
    fun addAccount() = runTest {
        val manager = buildManager()

        assertNotNull(manager.createAccount("foo", "bar", "", "", Source.LOCAL))
    }

    @Test
    fun findById() = runTest {
        val manager = buildManager()

        val accountID = manager.createAccount("foo", "bar", "", "", Source.LOCAL)

        val account = manager.findByID(accountID)

        assertEquals(accountID, account!!.id)
    }

    @Test
    fun findByIdMissingAccount() = runTest {
        val manager = buildManager()

        assertNull(manager.findByID("bogus"))
    }
}
