package com.capyreader.app.ui.components.pullrefresh

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class SwipeRefreshNestedScrollConnectionTest {
    @Test
    fun slowBottomPullMovesIndicator() {
        val state = SwipeRefreshState(isRefreshing = false)
        val connection = connection(state, scrollFromTop = false)

        repeat(100) {
            connection.postScroll(y = -0.3f)
        }

        assertEquals(18f, state.indicatorOffset, 0.01f)
    }

    @Test
    fun slowTopPullMovesIndicator() {
        val state = SwipeRefreshState(isRefreshing = false)
        val connection = connection(state, scrollFromTop = true)

        repeat(100) {
            connection.postScroll(y = 0.3f)
        }

        assertEquals(18f, state.indicatorOffset, 0.01f)
    }

    @Test
    fun slowBottomReverseRetractsIndicator() {
        val state = SwipeRefreshState(isRefreshing = false)
        val connection = connection(state, scrollFromTop = false)

        connection.postScroll(y = -10f)

        repeat(10) {
            connection.preScroll(y = 0.3f)
        }

        assertEquals(4.2f, state.indicatorOffset, 0.01f)
    }

    @Test
    fun slowTopReverseRetractsIndicator() {
        val state = SwipeRefreshState(isRefreshing = false)
        val connection = connection(state, scrollFromTop = true)

        connection.postScroll(y = 10f)

        repeat(10) {
            connection.preScroll(y = -0.3f)
        }

        assertEquals(4.2f, state.indicatorOffset, 0.01f)
    }

    @Test
    fun retractStopsAtZeroAndReturnsTheRest() {
        val state = SwipeRefreshState(isRefreshing = false)
        val connection = connection(state, scrollFromTop = false)

        connection.postScroll(y = -1f)
        val consumed = connection.preScroll(y = 3f)

        assertEquals(0f, state.indicatorOffset, 0.001f)
        assertEquals(1f, consumed.y, 0.001f)
    }

    @Test
    fun releaseAtTriggerRefreshes() = runTest {
        val state = SwipeRefreshState(isRefreshing = false)
        var refreshes = 0
        val connection = connection(state, scrollFromTop = false, onRefresh = { refreshes += 1 })

        repeat(500) {
            connection.postScroll(y = -0.4f)
        }
        connection.onPreFling(Velocity.Zero)

        assertEquals(120f, state.indicatorOffset, 0.01f)
        assertEquals(1, refreshes)
        assertFalse(state.isSwipeInProgress)
    }

    @Test
    fun releaseBelowTriggerDoesNotRefresh() = runTest {
        val state = SwipeRefreshState(isRefreshing = false)
        var refreshes = 0
        val connection = connection(state, scrollFromTop = false, onRefresh = { refreshes += 1 })

        repeat(400) {
            connection.postScroll(y = -0.4f)
        }
        connection.onPreFling(Velocity.Zero)

        assertEquals(96f, state.indicatorOffset, 0.01f)
        assertEquals(0, refreshes)
    }

    private fun connection(
        state: SwipeRefreshState,
        scrollFromTop: Boolean,
        onRefresh: () -> Unit = {},
    ) =
        SwipeRefreshNestedScrollConnection(
            state = state,
            onRefresh = onRefresh,
            scrollFromTop = scrollFromTop,
        ).apply {
            enabled = true
            refreshTrigger = 100f
        }

    private fun SwipeRefreshNestedScrollConnection.postScroll(y: Float) = onPostScroll(
        consumed = Offset.Zero,
        available = Offset(x = 0f, y = y),
        source = NestedScrollSource.UserInput,
    )

    private fun SwipeRefreshNestedScrollConnection.preScroll(y: Float) = onPreScroll(
        available = Offset(x = 0f, y = y),
        source = NestedScrollSource.UserInput,
    )
}
