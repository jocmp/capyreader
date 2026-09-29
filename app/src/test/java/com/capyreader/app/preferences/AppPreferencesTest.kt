package com.capyreader.app.preferences

import androidx.preference.PreferenceManager
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.articles.SortOrder
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class AppPreferencesTest {
    private lateinit var appPreferences: AppPreferences

    @Before
    fun setUp() {
        appPreferences = AppPreferences(RuntimeEnvironment.getApplication()).also {
            it.clearAll()
        }
    }

    @After
    fun tearDown() {
        stopKoin()
    }

    @Test
    fun `sort order defaults to newest first for each status`() {
        ArticleStatus.entries.forEach { status ->
            assertEquals(
                SortOrder.NEWEST_FIRST,
                appPreferences.articleListOptions.getSortOrder(status).get()
            )
        }
    }

    @Test
    fun `sort order falls back to the legacy sort order`() {
        PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.getApplication())
            .edit()
            .putString("article_list_sort_order", SortOrder.OLDEST_FIRST.name)
            .commit()

        ArticleStatus.entries.forEach { status ->
            assertEquals(
                SortOrder.OLDEST_FIRST,
                appPreferences.articleListOptions.getSortOrder(status).get()
            )
        }
    }

    @Test
    fun `sort order is independent per status`() {
        appPreferences.articleListOptions.getSortOrder(ArticleStatus.UNREAD).set(SortOrder.OLDEST_FIRST)

        assertEquals(
            SortOrder.NEWEST_FIRST,
            appPreferences.articleListOptions.getSortOrder(ArticleStatus.ALL).get()
        )
        assertEquals(
            SortOrder.OLDEST_FIRST,
            appPreferences.articleListOptions.getSortOrder(ArticleStatus.UNREAD).get()
        )
        assertEquals(
            SortOrder.NEWEST_FIRST,
            appPreferences.articleListOptions.getSortOrder(ArticleStatus.STARRED).get()
        )
    }
}
