package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.layout.offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp

/**
 * A [NestedScrollConnection] that provides scroll events to a hoisted [state].
 *
 * Note that this modifier must be added above a scrolling container using [Modifier.nestedScroll],
 * such as a lazy column, in order to receive scroll events.
 *
 * And you should manually handle the offset of components
 * with [PullToLoadState.absProgress] or [PullToLoadState.offsetFraction]
 *
 * @param enabled If not enabled, all scroll delta and fling velocity will be ignored.
 * @param onScroll Used for detecting if the reader is scrolling down
 */
private class ReaderNestedScrollConnection(
    private val enabled: Boolean,
    private val onPreScroll: (Float) -> Float,
    private val onPostScroll: (Float) -> Float,
    private val onRelease: () -> Unit,
    private val onScroll: ((Float) -> Unit)? = null
) : NestedScrollConnection {

    override fun onPreScroll(
        available: Offset, source: NestedScrollSource
    ): Offset {
        onScroll?.invoke(available.y)
        return when {
            !enabled || available.y == 0f -> Offset.Zero

            // Scroll down to reduce the progress when the offset is currently pulled up, same for the opposite
            source == NestedScrollSource.UserInput -> {
                Offset(0f, onPreScroll(available.y))
            }

            else -> Offset.Zero
        }
    }

    override fun onPostScroll(
        consumed: Offset, available: Offset, source: NestedScrollSource
    ): Offset = when {
        !enabled -> Offset.Zero
        source == NestedScrollSource.UserInput -> Offset(
            0f,
            onPostScroll(available.y)
        ) // Pull to load
        else -> Offset.Zero
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        onRelease()
        return Velocity.Zero
    }
}

fun Modifier.pullToLoad(
    state: PullToLoadState,
    contentOffsetY: Density.(Float) -> Int = { fraction ->
        (PullToLoadDefaults.ContentOffsetMultiple.dp * fraction).roundToPx()
    },
    onScroll: ((Float) -> Unit)? = null,
    enabled: Boolean = true,
): Modifier =
    nestedScroll(
        ReaderNestedScrollConnection(
            enabled = enabled,
            onPreScroll = state::onPullBack,
            onPostScroll = state::onPull,
            onRelease = state::onRelease,
            onScroll = onScroll
        )
    ).then(
        if (enabled) Modifier.offset {
            IntOffset(x = 0, y = contentOffsetY(state.offsetFraction))
        }
        else this
    )
