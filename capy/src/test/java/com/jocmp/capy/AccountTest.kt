package com.jocmp.capy

import com.jocmp.capy.accounts.AutoDelete
import com.jocmp.capy.common.nowUTC
import com.jocmp.capy.fixtures.AccountFixture
import com.jocmp.capy.fixtures.ArticleFixture
import io.mockk.coEvery
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class AccountTest {
    @JvmField
    @Rule
    val folder = TemporaryFolder()

    private lateinit var account: Account

    @Before
    fun setup() {
        account = AccountFixture.create(parentFolder = folder)
        coEvery { account.delegate.refresh(any()) }.returns(Result.success(Unit))
    }

    @Test
    fun refresh() = runTest {
        val oldArticle = ArticleFixture(database = account.database).create(
            publishedAt = nowUTC().minusMonths(4).toEpochSecond()
        )

        assertEquals(account.refresh(), Result.success(Unit))

        assertNull(account.database.reload(oldArticle))
    }

    @Test
    fun refresh_autoDeleteDisabled() = runTest {
        val oldArticle = ArticleFixture(database = account.database).create(
            publishedAt = nowUTC().minusMonths(4).toEpochSecond()
        )
        account.preferences.autoDelete.set(AutoDelete.DISABLED)

        assertEquals(account.refresh(), Result.success(Unit))
        assertNotNull(account.database.reload(oldArticle))
    }
}
