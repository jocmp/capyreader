package com.capyreader.app.ui.articles

import android.app.Application
import app.cash.turbine.test
import com.capyreader.app.notifications.NotificationHelper
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.ArticleListVerticalSwipe
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.ui.articles.feeds.AngleRefreshState
import com.jocmp.capy.Account
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.ArticleStatus
import com.jocmp.capy.Feed
import com.jocmp.capy.Folder
import com.jocmp.capy.accounts.Source
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
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
    fun `refreshAll transitions from stopped, running, to settling`() = runTest {
        appPreferences.refreshInterval.set(RefreshInterval.MANUALLY_ONLY)

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
        appPreferences.refreshInterval.set(RefreshInterval.MANUALLY_ONLY)

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
    fun `requestNextFeed opens next feed after current feed's unread count drops to zero`() = runTest {
        val initialFilter = ArticleFilter.Feeds(
            feedID = "1",
            folderTitle = null,
            feedStatus = ArticleStatus.UNREAD
        )
        appPreferences.filter.set(initialFilter)
        appPreferences.articleListOptions.swipeBottom.set(ArticleListVerticalSwipe.NEXT_FEED)

        val feedA = Feed(id = "1", subscriptionID = "1", title = "A", feedURL = "a", count = 3)
        val feedB = Feed(id = "2", subscriptionID = "2", title = "B", feedURL = "b", count = 3)

        val feeds = MutableStateFlow(listOf(feedA, feedB))
        val counts = MutableStateFlow<Map<String, Long>>(mapOf("1" to 3, "2" to 3))

        every { account.feeds } returns feeds
        every { account.countAll(any()) } returns counts

        val viewModel = buildViewModel()
        advanceUntilIdle()

        counts.value = mapOf("1" to 0, "2" to 3)
        advanceUntilIdle()

        viewModel.requestNextFeed()
        advanceUntilIdle()

        val expectedNext = ArticleFilter.Feeds(
            feedID = "2",
            folderTitle = null,
            feedStatus = ArticleStatus.UNREAD
        )
        assertEquals(expectedNext, appPreferences.filter.get())
    }

    @Test
    fun `requestNextFeed skips empty children when current folder is marked read`() = runTest {
        val initialFilter = ArticleFilter.Folders(
            folderTitle = "X",
            folderStatus = ArticleStatus.UNREAD,
        )
        appPreferences.filter.set(initialFilter)
        appPreferences.articleListOptions.swipeBottom.set(ArticleListVerticalSwipe.NEXT_FEED)

        val x1 = Feed(id = "x1", subscriptionID = "x1", title = "X1", feedURL = "x1", folderName = "X")
        val x2 = Feed(id = "x2", subscriptionID = "x2", title = "X2", feedURL = "x2", folderName = "X")
        val y1 = Feed(id = "y1", subscriptionID = "y1", title = "Y1", feedURL = "y1", folderName = "Y")

        val folderX = Folder(title = "X", feeds = listOf(x1, x2), expanded = true)
        val folderY = Folder(title = "Y", feeds = listOf(y1), expanded = true)

        val folders = MutableStateFlow(listOf(folderX, folderY))
        val counts = MutableStateFlow<Map<String, Long>>(
            mapOf("x1" to 2L, "x2" to 2L, "y1" to 3L)
        )

        every { account.folders } returns folders
        every { account.feeds } returns flowOf(emptyList())
        every { account.countAll(any()) } returns counts

        val viewModel = buildViewModel()
        advanceUntilIdle()

        counts.value = mapOf("x1" to 0L, "x2" to 0L, "y1" to 3L)
        advanceUntilIdle()

        viewModel.requestNextFeed()
        advanceUntilIdle()

        assertEquals(
            ArticleFilter.Folders(folderTitle = "Y", folderStatus = ArticleStatus.UNREAD),
            appPreferences.filter.get()
        )
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
        )
    }
}
