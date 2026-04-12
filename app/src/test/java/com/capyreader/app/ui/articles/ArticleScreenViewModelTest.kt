package com.capyreader.app.ui.articles

import android.app.Application
import app.cash.turbine.test
import com.capyreader.app.notifications.NotificationHelper
import com.capyreader.app.preferences.AfterReadAllBehavior
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.preferences.ArticleListVerticalSwipe
import com.capyreader.app.refresher.RefreshInterval
import com.capyreader.app.ui.articles.feeds.AngleRefreshState
import com.jocmp.capy.Account
import com.jocmp.capy.ArticleFilter
import com.jocmp.capy.accounts.Source
import com.jocmp.capy.articles.SortOrder
import com.jocmp.capy.preferences.Preference
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
            every { canSaveArticleExternally } returns mockPreference(false)
            every { source } returns Source.LOCAL
            coEvery { refresh(any()) } returns Result.success(Unit)
        }

        appPreferences = mockk(relaxed = true) {
            every { filter } returns mockPreference(ArticleFilter.default())
            every { refreshInterval } returns mockPreference(RefreshInterval.EVERY_TWO_HOURS)
            every { enableStickyFullContent } returns mockPreference(false)
            every { articleListOptions } returns mockk(relaxed = true) {
                every { swipeBottom } returns mockPreference(ArticleListVerticalSwipe.DISABLED)
                every { sortOrder } returns mockPreference(SortOrder.default)
                every { afterReadAllBehavior } returns mockPreference(AfterReadAllBehavior.default)
            }
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
    fun `skips initial refresh when refresh interval is manual only`() = runTest {
        every { appPreferences.refreshInterval } returns mockPreference(RefreshInterval.MANUALLY_ONLY)

        val viewModel = buildViewModel()

        assertTrue(viewModel.refreshInitialized)
        assertFalse(viewModel.refreshingAll)
    }

    @Test
    fun `refreshAll transitions from stopped, running, to settling`() = runTest {
        every { appPreferences.refreshInterval } returns mockPreference(RefreshInterval.MANUALLY_ONLY)

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
        every { appPreferences.refreshInterval } returns mockPreference(RefreshInterval.MANUALLY_ONLY)

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

    private fun buildViewModel(): ArticleScreenViewModel {
        val application = RuntimeEnvironment.getApplication() as Application

        return ArticleScreenViewModel(
            account = account,
            appPreferences = appPreferences,
            application = application,
            notificationHelper = notificationHelper,
        )
    }

    private inline fun <reified T> mockPreference(value: T): Preference<T> {
        return mockk {
            every { get() } returns value
            every { stateIn(any()) } returns MutableStateFlow(value)
            every { changes() } returns flowOf(value)
        }
    }
}
