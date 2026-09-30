package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.input.pointer.changedToUp
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.articles.reader.PageDirection
import com.capyreader.app.ui.articles.reader.PageInsets
import com.capyreader.app.ui.collectChangesWithCurrent
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PageTurnGestures(
    scrollState: ScrollState,
    pinToolbars: Boolean,
    showToolbars: Boolean,
    onHideToolbars: () -> Unit,
    onToggleToolbars: () -> Unit,
    onTurnPastArticle: (PageDirection) -> Unit,
    appPreferences: AppPreferences = koinInject(),
    content: @Composable () -> Unit,
) {
    val enableTaps by appPreferences.readerOptions.enablePagingTapGesture.collectChangesWithCurrent()
    val enableKeys by appPreferences.readerOptions.enablePageTurnKeys.collectChangesWithCurrent()
    val enableScrollbar by appPreferences.readerOptions.enableEInkScrollbar.collectChangesWithCurrent()
    val visibleInsets by rememberUpdatedState(pageInsets(pinToolbars, showToolbars))
    val hiddenInsets by rememberUpdatedState(pageInsets(pinToolbars, showToolbars = false))
    val currentOnHideToolbars by rememberUpdatedState(onHideToolbars)
    val currentOnTurnPastArticle by rememberUpdatedState(onTurnPastArticle)
    val scope = rememberCoroutineScope()
    val keys = LocalPageTurnKeys.current

    val turn = remember(scrollState, scope) {
        { direction: PageDirection ->
            scope.launch {
                val current = scrollState.value
                val viewport = scrollState.viewportSize.toFloat()

                if (direction == PageDirection.FORWARD) {
                    val step = pageStep(
                        viewport = viewport,
                        leading = visibleInsets.bottom,
                        trailing = hiddenInsets.top,
                    )
                    scrollState.scrollBy(step)
                } else {
                    val step = pageStep(
                        viewport = viewport,
                        leading = visibleInsets.top,
                        trailing = visibleInsets.bottom,
                    )
                    scrollState.scrollBy(-step)
                }

                if (scrollState.value == current) {
                    currentOnTurnPastArticle(direction)
                } else if (direction == PageDirection.FORWARD) {
                    currentOnHideToolbars()
                }
            }
            Unit
        }
    }

    DisposableEffect(keys, enableKeys, turn) {
        if (!enableKeys) {
            return@DisposableEffect onDispose {}
        }

        val unregister = keys.register(turn)

        onDispose { unregister() }
    }

    Row {
        Box(
            modifier = Modifier
                .weight(1f)
                .pageTapZones(
                    enabled = enableTaps,
                    onTurn = turn,
                    onCenterTap = onToggleToolbars,
                )
        ) {
            content()
        }

        if (enableScrollbar) {
            EInkScrollbar(
                state = rememberEInkScrollbarState(scrollState),
                onPage = turn,
                onLine = turn,
                modifier = Modifier.padding(scrollbarPadding(pinToolbars)),
            )
        }
    }
}

fun pageStep(viewport: Float, leading: Float, trailing: Float): Float {
    val visible = (viewport - leading - trailing).coerceAtLeast(0f)

    return visible * PAGE_PROPORTION
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun scrollbarPadding(pinToolbars: Boolean): PaddingValues {
    if (pinToolbars) {
        return PaddingValues()
    }

    return PaddingValues(
        top = ArticleBarDefaults.topBarOffset,
        bottom = ArticleBarDefaults.BottomBarHeight +
                WindowInsets.navigationBarsIgnoringVisibility.asPaddingValues().calculateBottomPadding(),
    )
}

private fun Modifier.pageTapZones(
    enabled: Boolean,
    onTurn: (PageDirection) -> Unit,
    onCenterTap: () -> Unit,
): Modifier {
    if (!enabled) {
        return this
    }

    return pointerInput(onTurn, onCenterTap) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Initial)
            val direction = edgeDirection(down.position.x / size.width)

            if (direction != null) {
                val up = awaitEdgeTap(down) ?: return@awaitEachGesture

                up.consume()
                onTurn(direction)
            } else if (awaitUnhandledTap(down)) {
                onCenterTap()
            }
        }
    }
}

private suspend fun AwaitPointerEventScope.awaitEdgeTap(down: PointerInputChange): PointerInputChange? {
    return awaitTap(down, PointerEventPass.Initial)
}

private suspend fun AwaitPointerEventScope.awaitUnhandledTap(down: PointerInputChange): Boolean {
    val up = awaitTap(down, PointerEventPass.Final) ?: return false

    return !up.isConsumed
}

private suspend fun AwaitPointerEventScope.awaitTap(
    down: PointerInputChange,
    pass: PointerEventPass,
): PointerInputChange? {
    while (true) {
        val event = awaitPointerEvent(pass)
        val change = event.changes.firstOrNull { it.id == down.id } ?: return null
        val distance = (change.position - down.position).getDistance()

        if (distance > viewConfiguration.touchSlop || isLongPress(down, change)) {
            return null
        }

        if (change.changedToUp()) {
            return change
        }
    }
}

private fun AwaitPointerEventScope.isLongPress(down: PointerInputChange, change: PointerInputChange): Boolean {
    return change.uptimeMillis - down.uptimeMillis > viewConfiguration.longPressTimeoutMillis
}

private fun edgeDirection(zone: Float): PageDirection? {
    if (zone < EDGE_ZONE) {
        return PageDirection.BACK
    }

    if (zone > 1f - EDGE_ZONE) {
        return PageDirection.FORWARD
    }

    return null
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun pageInsets(pinToolbars: Boolean, showToolbars: Boolean): PageInsets {
    if (pinToolbars) {
        return PageInsets(top = 0f, bottom = 0f)
    }

    val statusBar = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    var top: Dp = statusBar
    var bottom: Dp = 0.dp

    if (showToolbars) {
        val navigationBar = WindowInsets.navigationBarsIgnoringVisibility
            .asPaddingValues()
            .calculateBottomPadding()

        top = ArticleBarDefaults.topBarOffset
        bottom = ArticleBarDefaults.BottomBarHeight + navigationBar
    }

    return with(LocalDensity.current) {
        PageInsets(top = top.toPx(), bottom = bottom.toPx())
    }
}

private const val EDGE_ZONE = 1f / 4f

private const val PAGE_PROPORTION = 0.96f
