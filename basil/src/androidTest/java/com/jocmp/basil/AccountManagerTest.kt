package com.jocmp.basil

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before

@RunWith(AndroidJUnit4::class)
class AccountManagerTest {
    @Before
    fun beforeEach() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        AccountManager(appContext).clearAll()
    }

    @Test
    fun addAccount() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val manager = AccountManager(appContext)
        assertNotNull(manager.createAccount())
        assertEquals(1, manager.accounts.size)
    }

    @Test
    fun removeAccount() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext

        val manager = AccountManager(appContext)

        val account = manager.createAccount()
        manager.createAccount()

        manager.removeAccount(account)

        assertEquals(1, manager.accounts.size)
    }
}
