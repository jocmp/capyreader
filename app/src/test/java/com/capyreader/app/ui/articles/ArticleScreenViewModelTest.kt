package com.capyreader.app.ui.articles

import android.app.Application
import app.cash.turbine.test
import com.capyreader.app.notifications.NotificationHelper
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.AfterReadAllBehavior
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.ui.articles.feeds.AngleRefreshState
import com.jocmp.capy.Account
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.articles.SortOrder
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.time.ZonedDateTime
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class ArticleScreenViewModelTest {
    private val testDispatcher = StandardTestDispatcher()

    private lateinit var account: Account
    private lateinit var appPreferences: AppPreferences
    private lateinit var notificationHelper: NotificationHelper

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        account = mockk(relaxed = true) {
            every { folders } returns flowOf(emptyList())
            every { feeds } returns flowOf(emptyList())
            every { taggedFeeds } returns flowOf(emptyList())
            every { savedSearches } returns flowOf(emptyList())
            every { canSaveArticleExternally } returns mockk(relaxed = true) {
                every { get() } returns false
                every { stateIn(any()) } returns MutableStateFlow(false)
                every { changes() } returns flowOf(false)
            }
            every { countAll(any()) } returns flowOf(emptyMap())
            every { countAllBySavedSearch(any()) } returns flowOf(emptyMap())
            every { source } returns Source.LOCAL
            coEvery { refresh(any()) } returns Result.success(Unit)
            every { preferences } returns mockk(relaxed = true) {
                every { lastRefreshedAt } returns mockk(relaxed = true) {
                    every { get() } returns 0L
                }
            }
        }

        appPreferences = AppPreferences(RuntimeEnvironment.getApplication()).also {
            it.clearAll()
            it.refreshInterval.set(RefreshInterval.EVERY_TWO_HOURS)
        }

        notificationHelper = mockk(relaxed = true)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
        stopKoin()
    }

    @Test
    fun `refreshAll triggers initial refresh`() = runTest {
        val viewModel = buildViewModel()

        assertFalse(viewModel.refreshInitialized)

        advanceUntilIdle()

        assertTrue(viewModel.refreshInitialized)
    }

    @Test
    fun `skips initial refresh when account has already synced before`() = runTest {
        every { account.preferences.lastRefreshedAt.get() } returns
            ZonedDateTime.parse("2023-11-14T22:13:20Z").toEpochSecond()

        val viewModel = buildViewModel()

        assertTrue(viewModel.refreshInitialized)
        assertFalse(viewModel.refreshingAll)
    }

    @Test
    fun `shows onboarding for a local account without feeds`() = runTest {
        val viewModel = buildViewModel()

        viewModel.showOnboarding.test {
            assertTrue(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `skips onboarding for a hosted account without feeds`() = runTest {
        every { account.source } returns Source.FEEDBIN

        val viewModel = buildViewModel()

        viewModel.showOnboarding.test {
            assertFalse(awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refreshAll transitions from stopped, running, to settling`() = runTest {
        every { account.preferences.lastRefreshedAt.get() } returns
            ZonedDateTime.parse("2023-11-14T22:13:20Z").toEpochSecond()

        val viewModel = buildViewModel()

        viewModel.refreshAllState.test {
            assertEquals(AngleRefreshState.STOPPED, awaitItem())

            viewModel.refreshAll()
            assertEquals(AngleRefreshState.RUNNING, awaitItem())

            advanceUntilIdle()

            assertEquals(AngleRefreshState.SETTLING, awaitItem())
        }
    }

    @Test
    fun `refreshAll guards against double calls while running`() = runTest {
        every { account.preferences.lastRefreshedAt.get() } returns
            ZonedDateTime.parse("2023-11-14T22:13:20Z").toEpochSecond()

        val viewModel = buildViewModel()

        viewModel.refreshAllState.test {
            assertEquals(AngleRefreshState.STOPPED, awaitItem())

            viewModel.refreshAll()
            assertEquals(AngleRefreshState.RUNNING, awaitItem())

            viewModel.refreshAll()

            advanceUntilIdle()

            assertEquals(AngleRefreshState.SETTLING, awaitItem())
        }
    }

    @Test
    fun `selectStatus updates the filter without waiting on preferences`() = runTest {
        appPreferences.filter.set(ArticleFilter.Articles(articleStatus = ArticleStatus.STARRED))

        val viewModel = buildViewModel()
        advanceUntilIdle()

        val starredList = viewModel.articleList.value

        viewModel.selectStatus(ArticleStatus.UNREAD)

        val expected = ArticleFilter.Articles(articleStatus = ArticleStatus.UNREAD)
        assertEquals(expected, viewModel.filter.value)
        assertEquals(expected, appPreferences.filter.get())

        advanceUntilIdle()

        assertEquals(expected, viewModel.articleList.value.filter)
        assertTrue(starredList !== viewModel.articleList.value)
    }

    @Test
    fun `applies filters persisted outside the view model`() = runTest {
        val viewModel = buildViewModel()
        advanceUntilIdle()

        val deepLinked = ArticleFilter.Feeds(
            feedID = "1",
            folderTitle = null,
            feedStatus = ArticleStatus.UNREAD
        )
        appPreferences.filter.set(deepLinked)
        advanceUntilIdle()

        assertEquals(deepLinked, viewModel.filter.value)
    }

    @Test
    fun `markAllRead uses the sort order for the filter status`() = runTest {
        appPreferences.articleListOptions.getSortOrder(ArticleStatus.UNREAD).set(SortOrder.OLDEST_FIRST)
        val unread = ArticleFilter.Articles(articleStatus = ArticleStatus.UNREAD)
        val starred = ArticleFilter.Articles(articleStatus = ArticleStatus.STARRED)
        appPreferences.filter.set(unread)

        val viewModel = buildViewModel()

        viewModel.markAllRead(filter = unread)
        viewModel.markAllRead(filter = starred)
        advanceUntilIdle()

        verify {
            account.unreadArticleIDs(
                filter = unread,
                range = any(),
                sortOrder = SortOrder.OLDEST_FIRST,
                query = any(),
                since = any(),
            )
        }
        verify {
            account.unreadArticleIDs(
                filter = starred,
                range = any(),
                sortOrder = SortOrder.NEWEST_FIRST,
                query = any(),
                since = any(),
            )
        }
    }

    @Test
    fun `markAllRead hands off to the feed list when opening the next feed`() = runTest {
        appPreferences.articleListOptions.afterReadAllBehavior.set(AfterReadAllBehavior.OPEN_NEXT_FEED)

        val viewModel = buildViewModel()
        advanceUntilIdle()

        // Runs on the IO dispatcher, outside the test scheduler.
        val openedNext = CountDownLatch(1)
        viewModel.markAllRead(onOpenNextFeed = { openedNext.countDown() })

        assertTrue(openedNext.await(5, TimeUnit.SECONDS))
    }

    private fun buildViewModel(
        syncFlushInterval: kotlin.time.Duration? = null,
    ): ArticleScreenViewModel {
        val application = RuntimeEnvironment.getApplication() as Application

        return ArticleScreenViewModel(
            account = account,
            appPreferences = appPreferences,
            application = application,
            notificationHelper = notificationHelper,
            ioDispatcher = testDispatcher,
            syncFlushInterval = syncFlushInterval,
            articleCutoff = ArticleSessionCutoff(),
        )
    }
}
