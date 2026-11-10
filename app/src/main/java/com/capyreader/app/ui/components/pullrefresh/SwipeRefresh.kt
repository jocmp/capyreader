package com.capyreader.app.ui.components.pullrefresh

import androidx.annotation.VisibleForTesting
import androidx.compose.animation.core.animate
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.BiasAlignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope

private const val DragMultiplier = 0.6f

private const val TriggerRearmFraction = 0.9f

/**
 * Creates a [SwipeRefreshState] that is remembered across compositions.
 *
 * Changes to [isRefreshing] will result in the [SwipeRefreshState] being updated.
 *
 * @param isRefreshing the value for [SwipeRefreshState.isRefreshing]
 */
@Composable
fun rememberSwipeRefreshState(
    isRefreshing: Boolean = false,
): SwipeRefreshState {
    return remember {
        SwipeRefreshState(
            isRefreshing = isRefreshing,
        )
    }.apply {
        this.isRefreshing = isRefreshing
    }
}

/**
 * A state object that can be hoisted to control and observe changes for [SwipeRefresh].
 *
 * In most cases, this will be created via [rememberSwipeRefreshState].
 *
 * @param isRefreshing the initial value for [SwipeRefreshState.isRefreshing]
 */
@Stable
class SwipeRefreshState(
    isRefreshing: Boolean,
) {
    private var offset by mutableFloatStateOf(0f)
    private var settle: Job? = null

    /**
     * Whether this [SwipeRefreshState] is currently refreshing or not.
     */
    var isRefreshing: Boolean by mutableStateOf(isRefreshing)

    /**
     * Whether a swipe/drag is currently in progress.
     */
    var isSwipeInProgress: Boolean by mutableStateOf(false)
        internal set

    /**
     * The current offset for the indicator, in pixels.
     */
    val indicatorOffset: Float get() = offset

    internal suspend fun animateOffsetTo(target: Float) = coroutineScope {
        val job = coroutineContext[Job]
        settle = job
        try {
            animate(initialValue = offset, targetValue = target) { value, _ ->
                // A frame already queued when dragBy cancels must not overwrite the drag.
                if (job?.isActive == true) {
                    offset = value
                }
            }
        } finally {
            if (settle === job) {
                settle = null
            }
        }
    }

    /**
     * Applies a drag delta in pixels synchronously, so the next scroll event
     * reads the updated offset. Interrupts any settle animation.
     */
    internal fun dragBy(delta: Float) {
        settle?.cancel()
        settle = null
        offset += delta
    }
}

@VisibleForTesting(otherwise = VisibleForTesting.PRIVATE)
internal class SwipeRefreshNestedScrollConnection(
    private val state: SwipeRefreshState,
    private val onRefresh: () -> Unit,
    private val scrollFromTop: Boolean = true,
) : NestedScrollConnection {
    var enabled: Boolean = false
    var refreshTrigger: Float = 0f

    override fun onPreScroll(
        available: Offset,
        source: NestedScrollSource
    ): Offset = when {
        // If swiping isn't enabled, return zero
        !enabled -> Offset.Zero
        // If we're refreshing, return zero
        state.isRefreshing -> Offset.Zero
        // If the user is swiping and there's y remaining, handle it
        scrollFromTop && source == NestedScrollSource.UserInput && available.y < 0f -> onScroll(available)
        !scrollFromTop && source == NestedScrollSource.UserInput && available.y > 0f -> onScroll(
            available
        )

        else -> Offset.Zero
    }

    override fun onPostScroll(
        consumed: Offset,
        available: Offset,
        source: NestedScrollSource
    ): Offset = when {
        // If swiping isn't enabled, return zero
        !enabled -> Offset.Zero
        // If we're refreshing, return zero
        state.isRefreshing -> Offset.Zero
        // If the user is swiping and there's y remaining, handle it
        scrollFromTop && source == NestedScrollSource.UserInput && available.y > 0f -> onScroll(available)
        !scrollFromTop && source == NestedScrollSource.UserInput && available.y < 0f -> onScroll(
            available
        )

        else -> Offset.Zero
    }

    private fun onScroll(available: Offset): Offset {
        state.isSwipeInProgress = true

        val dragConsumed = getDragConsumed(available)

        // No minimum delta: a slow finger moves less than a pixel per event,
        // and dropping those deltas made the indicator stall and jump.
        return if (dragConsumed != 0f) {
            state.dragBy(dragConsumed)
            // Return the consumed Y
            if (scrollFromTop) {
                Offset(x = 0f, y = dragConsumed / DragMultiplier)
            } else {
                Offset(x = 0f, y = -dragConsumed / DragMultiplier)
            }
        } else {
            Offset.Zero
        }
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        // If we're dragging, not currently refreshing and scrolled
        // past the trigger point, refresh!
        if (!state.isRefreshing && state.indicatorOffset >= refreshTrigger) {
            onRefresh()
        }

        // Reset the drag in progress state
        state.isSwipeInProgress = false

        // Don't consume any velocity, to allow the scrolling layout to fling
        return Velocity.Zero
    }

    private fun getDragConsumed(available: Offset): Float {
        return if (scrollFromTop) {
            val newOffset = (available.y * DragMultiplier + state.indicatorOffset).coerceAtLeast(0f)
            newOffset - state.indicatorOffset
        } else {
            val newOffset = (available.y * DragMultiplier - state.indicatorOffset).coerceAtMost(0f)
            -(newOffset + state.indicatorOffset)
        }
    }
}

/**
 * A layout which implements the swipe-to-refresh pattern, allowing the user to refresh content via
 * a vertical swipe gesture.
 *
 * This layout requires its content to be scrollable so that it receives vertical swipe events.
 * The scrollable content does not need to be a direct descendant though. Layouts such as
 * [androidx.compose.foundation.lazy.LazyColumn] are automatically scrollable, but others such as
 * [androidx.compose.foundation.layout.Column] require you to provide the
 * [androidx.compose.foundation.verticalScroll] modifier to that content.
 *
 * Apps should provide a [onRefresh] block to be notified each time a swipe to refresh gesture
 * is completed. That block is responsible for updating the [state] as appropriately,
 * typically by setting [SwipeRefreshState.isRefreshing] to `true` once a 'refresh' has been
 * started. Once a refresh has completed, the app should then set
 * [SwipeRefreshState.isRefreshing] to `false`.
 *
 * If an app wishes to show the progress animation outside of a swipe gesture, it can
 * set [SwipeRefreshState.isRefreshing] as required.
 *
 * This layout does not clip any of it's contents, including the indicator. If clipping
 * is required, apps can provide the [androidx.compose.ui.draw.clipToBounds] modifier.
 *
 * @sample com.google.accompanist.sample.swiperefresh.SwipeRefreshSample
 *
 * @param state the state object to be used to control or observe the [SwipeRefresh] state.
 * @param onRefresh Lambda which is invoked when a swipe to refresh gesture is completed.
 * @param modifier the modifier to apply to this layout.
 * @param swipeEnabled Whether the the layout should react to swipe gestures or not.
 * @param refreshTriggerDistance The minimum swipe distance which would trigger a refresh.
 * @param indicatorAlignment The alignment of the indicator. Defaults to [Alignment.TopCenter].
 * @param indicatorPadding Content padding for the indicator, to inset the indicator in if required.
 * @param indicator the indicator that represents the current state. By default this
 * will use a [SwipeRefreshIndicator].
 * @param clipIndicatorToPadding Whether to clip the indicator to [indicatorPadding]. If false is
 * provided the indicator will be clipped to the [content] bounds. Defaults to true.
 * @param content The content containing a scroll composable.
 */
@Composable
fun SwipeRefresh(
    modifier: Modifier = Modifier,
    state: SwipeRefreshState = rememberSwipeRefreshState(),
    onRefresh: () -> Unit,
    onTriggerThreshold: () -> Unit,
    swipeEnabled: Boolean = true,
    refreshTriggerDistance: Dp = 80.dp,
    indicatorAlignment: Alignment = Alignment.TopCenter,
    indicatorPadding: PaddingValues = PaddingValues(0.dp),
    icon: ImageVector = Icons.Rounded.KeyboardArrowUp,
    indicator: @Composable (state: SwipeRefreshState, refreshTrigger: Dp) -> Unit = { s, trigger ->
        SwipeRefreshIndicator(
            state = s,
            refreshTriggerDistance = trigger,
            icon = icon,
            clockwise = (indicatorAlignment as BiasAlignment).verticalBias != 1f
        )
    },
    clipIndicatorToPadding: Boolean = true,
    content: @Composable () -> Unit,
) {
    val updatedOnRefresh = rememberUpdatedState(onRefresh)
    val updatedOnTriggerThreshold = rememberUpdatedState(onTriggerThreshold)
    val refreshTriggerPx = with(LocalDensity.current) { refreshTriggerDistance.toPx() }
    val updatedRefreshTriggerPx = rememberUpdatedState(refreshTriggerPx)

    // Our LaunchedEffect, which animates the indicator to its resting position
    LaunchedEffect(state.isSwipeInProgress) {
        if (!state.isSwipeInProgress) {
            // If there's not a swipe in progress, rest the indicator at 0f
            state.animateOffsetTo(0f)
        }
    }

    // Our nested scroll connection, which updates our state.
    val nestedScrollConnection = remember(state) {
        SwipeRefreshNestedScrollConnection(
            state = state,
            onRefresh = {
                // On refresh, re-dispatch to the update onRefresh block
                updatedOnRefresh.value.invoke()
            },
            scrollFromTop = (indicatorAlignment as BiasAlignment).verticalBias != 1f
        )
    }.apply {
        this.enabled = swipeEnabled
        this.refreshTrigger = refreshTriggerPx
    }

    // Re-arm only once the pull falls back below the trigger with some margin,
    // so sub-pixel finger noise at the threshold doesn't repeat the haptic.
    LaunchedEffect(state) {
        var armed = true
        snapshotFlow { state.indicatorOffset }.collect { offset ->
            val trigger = updatedRefreshTriggerPx.value
            if (armed && offset >= trigger) {
                armed = false
                updatedOnTriggerThreshold.value()
            } else if (!armed && offset < trigger * TriggerRearmFraction) {
                armed = true
            }
        }
    }

    Box(modifier.nestedScroll(connection = nestedScrollConnection)) {
        content()

        Box(
            Modifier
                // If we're not clipping to the padding, we use clipToBounds() before the padding()
                // modifier.
                .let { if (!clipIndicatorToPadding) it.clipToBounds() else it }
                .padding(indicatorPadding)
                .matchParentSize()
                // Else, if we're are clipping to the padding, we use clipToBounds() after
                // the padding() modifier.
                .let { if (clipIndicatorToPadding) it.clipToBounds() else it }
        ) {
            Box(Modifier.align(indicatorAlignment)) {
                indicator(state, refreshTriggerDistance)
            }
        }
    }
}
