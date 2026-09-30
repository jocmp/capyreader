package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.verticalDrag
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.KeyboardArrowUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.articles.reader.PageDirection
import kotlin.math.roundToInt

@Stable
interface EInkScrollbarState {
    val position: Float

    val visibleFraction: Float

    fun scrollTo(fraction: Float)
}

@Composable
fun rememberEInkScrollbarState(listState: LazyListState): EInkScrollbarState {
    return remember(listState) { LazyListScrollbarState(listState) }
}

@Composable
fun EInkScrollbar(
    state: EInkScrollbarState,
    onPage: (PageDirection) -> Unit,
    onLine: (PageDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        shape = RoundedCornerShape(ScrollbarCorner),
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
            .padding(vertical = 8.dp)
            .padding(end = 8.dp)
            .width(ScrollbarWidth)
            .fillMaxHeight(),
    ) {
        Column {
            ScrollbarButton(
                icon = Icons.Rounded.KeyboardArrowUp,
                contentDescription = stringResource(R.string.reader_scroll_line_up),
                onClick = { onLine(PageDirection.BACK) },
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ScrollbarTrack(
                state = state,
                onPage = onPage,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            ScrollbarButton(
                icon = Icons.Rounded.KeyboardArrowDown,
                contentDescription = stringResource(R.string.reader_scroll_line_down),
                onClick = { onLine(PageDirection.FORWARD) },
            )
        }
    }
}

@Composable
private fun ScrollbarButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxWidth()
            .height(ButtonHeight)
            .clickable(onClick = onClick),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun ScrollbarTrack(
    state: EInkScrollbarState,
    onPage: (PageDirection) -> Unit,
    modifier: Modifier = Modifier,
) {
    val currentOnPage by rememberUpdatedState(onPage)

    BoxWithConstraints(modifier = modifier) {
        val density = LocalDensity.current
        val trackHeight = constraints.maxHeight.toFloat()
        val minThumb = with(density) { MinThumbHeight.toPx() }.coerceAtMost(trackHeight)
        val thumbHeight = (trackHeight * state.visibleFraction).coerceIn(minThumb, trackHeight)
        val travel = (trackHeight - thumbHeight).coerceAtLeast(1f)
        val thumbTop = travel * state.position

        val currentThumbTop by rememberUpdatedState(thumbTop)
        val currentThumbHeight by rememberUpdatedState(thumbHeight)
        val currentTravel by rememberUpdatedState(travel)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .pointerInput(state) {
                    awaitEachGesture {
                        val down = awaitFirstDown()
                        val top = currentThumbTop
                        val onThumb = down.position.y in top..(top + currentThumbHeight)

                        if (!onThumb) {
                            val up = waitForUpOrCancellation() ?: return@awaitEachGesture

                            if (up.position.y < top) {
                                currentOnPage(PageDirection.BACK)
                            } else {
                                currentOnPage(PageDirection.FORWARD)
                            }
                            return@awaitEachGesture
                        }

                        val grab = down.position.y - top
                        down.consume()

                        verticalDrag(down.id) { change ->
                            change.consume()
                            state.scrollTo((change.position.y - grab) / currentTravel)
                        }
                    }
                }
        )

        Box(
            modifier = Modifier
                .offset { IntOffset(0, thumbTop.roundToInt()) }
                .fillMaxWidth()
                .height(with(density) { thumbHeight.toDp() })
                .padding(horizontal = 6.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(ThumbCorner))
                .background(MaterialTheme.colorScheme.outline)
        )
    }
}

private class LazyListScrollbarState(private val listState: LazyListState) : EInkScrollbarState {
    private val averageItemHeight: Float
        get() {
            val visible = listState.layoutInfo.visibleItemsInfo

            if (visible.isEmpty()) {
                return 0f
            }

            return visible.sumOf { it.size }.toFloat() / visible.size
        }

    private val contentHeight: Float
        get() = averageItemHeight * listState.layoutInfo.totalItemsCount

    private val viewportHeight: Float
        get() = listState.layoutInfo.viewportSize.height.toFloat()

    private val scrollRange: Float
        get() = (contentHeight - viewportHeight).coerceAtLeast(0f)

    override val position: Float
        get() {
            if (scrollRange <= 0f) {
                return 0f
            }

            val scrolled = listState.firstVisibleItemIndex * averageItemHeight +
                    listState.firstVisibleItemScrollOffset

            return (scrolled / scrollRange).coerceIn(0f, 1f)
        }

    override val visibleFraction: Float
        get() {
            if (contentHeight <= 0f) {
                return 1f
            }

            return (viewportHeight / contentHeight).coerceIn(0f, 1f)
        }

    override fun scrollTo(fraction: Float) {
        val target = fraction.coerceIn(0f, 1f) * scrollRange
        val scrolled = listState.firstVisibleItemIndex * averageItemHeight +
                listState.firstVisibleItemScrollOffset

        listState.dispatchRawDelta(target - scrolled)
    }
}

private val ScrollbarWidth = 28.dp

private val ButtonHeight = 40.dp

private val ScrollbarCorner = 14.dp

private val ThumbCorner = 8.dp

private val MinThumbHeight = 40.dp
