package com.capyreader.app.ui.components.pullrefresh

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SwipeRefreshSlowPullTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val state = SwipeRefreshState(isRefreshing = false)
    private var touchSlop = 0f
    private var density = 1f
    private var thresholdSignals = 0

    @Test
    fun subPixelPullMovesIndicator() {
        setContent(refreshTriggerDistance = 1000.dp)

        val offset = pull(stepsPx = listOf(0.3f))

        assertTracksFinger(offset)
    }

    @Test
    fun unevenSlowPullCountsEveryEvent() {
        setContent(refreshTriggerDistance = 1000.dp)

        val offset = pull(stepsPx = listOf(0.5f, 1.2f))

        assertTracksFinger(offset)
    }

    @Test
    fun jitterAtTriggerSignalsOnce() {
        setContent(refreshTriggerDistance = 40.dp)
        val trigger = 40 * density
        // Lands ~0.3px past the trigger, so each 1px wiggle crosses back below it.
        val toTrigger = touchSlop + trigger / DRAG_MULTIPLIER + 0.5f

        composeRule.onNodeWithTag(TAG).performTouchInput {
            down(center)
            moveBy(Offset(x = 0f, y = -toTrigger), delayMillis = 8)
            // Slow enough that every crossing lands in its own frame.
            repeat(20) {
                moveBy(Offset(x = 0f, y = 1f), delayMillis = 50)
                moveBy(Offset(x = 0f, y = -1f), delayMillis = 50)
            }
        }
        composeRule.waitForIdle()

        assertTrue("pull reached ${state.indicatorOffset}px", state.indicatorOffset >= trigger * 0.9f)
        assertEquals(1, thresholdSignals)

        composeRule.onNodeWithTag(TAG).performTouchInput { up() }
    }

    private fun setContent(refreshTriggerDistance: Dp) {
        composeRule.setContent {
            touchSlop = LocalViewConfiguration.current.touchSlop
            density = LocalDensity.current.density

            SwipeRefresh(
                state = state,
                onRefresh = {},
                onTriggerThreshold = { thresholdSignals += 1 },
                refreshTriggerDistance = refreshTriggerDistance,
                indicatorAlignment = Alignment.BottomCenter,
                modifier = Modifier.testTag(TAG),
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    Box(modifier = Modifier.height(48.dp))
                }
            }
        }
    }

    /**
     * Drags upward from the center of a list that can't scroll, so the whole
     * drag past touch slop goes to the bottom indicator. One move event every
     * 8ms, like a touchscreen. Returns the offset while the finger is still down.
     */
    private fun pull(stepsPx: List<Float>): Float {
        val moves = (TRAVEL_PX / stepsPx.average()).toInt()

        composeRule.onNodeWithTag(TAG).performTouchInput {
            down(center)
            (0 until moves).forEach { i ->
                moveBy(Offset(x = 0f, y = -stepsPx[i % stepsPx.size]), delayMillis = 8)
            }
        }
        composeRule.waitForIdle()
        val offset = state.indicatorOffset

        composeRule.onNodeWithTag(TAG).performTouchInput { up() }

        return offset
    }

    private fun assertTracksFinger(offset: Float) {
        val expected = (TRAVEL_PX - touchSlop) * DRAG_MULTIPLIER

        assertTrue("indicator moved ${offset}px, expected ~${expected}px", offset > expected * 0.9f)
    }

    private companion object {
        const val TAG = "pull"
        const val TRAVEL_PX = 300f
        const val DRAG_MULTIPLIER = 0.6f
    }
}
