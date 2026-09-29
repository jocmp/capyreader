package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import com.capyreader.app.ui.articles.reader.PageFooterTitle
import com.capyreader.app.R
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBarsIgnoringVisibility
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
import com.capyreader.app.ui.articles.reader.ReaderPages
import com.capyreader.app.ui.collectChangesWithDefault
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

@Composable
fun PageTurnGestures(
    pages: ReaderPages,
    title: String,
    paginate: Boolean,
    pagedInsets: PageInsets,
    pinToolbars: Boolean,
    showToolbars: Boolean,
    onHideToolbars: () -> Unit,
    onToggleToolbars: () -> Unit,
    onTurnPastArticle: (PageDirection) -> Unit,
    appPreferences: AppPreferences = koinInject(),
    content: @Composable () -> Unit,
) {
    val enableTaps by appPreferences.readerOptions.enablePagingTapGesture.collectChangesWithDefault()
    val enableKeys by appPreferences.readerOptions.enablePageTurnKeys.collectChangesWithDefault()
    val visibleInsets by rememberUpdatedState(pageInsets(pinToolbars, showToolbars))
    val hiddenInsets by rememberUpdatedState(pageInsets(pinToolbars, showToolbars = false))
    val currentPaginate by rememberUpdatedState(paginate)
    val currentOnHideToolbars by rememberUpdatedState(onHideToolbars)
    val currentOnTurnPastArticle by rememberUpdatedState(onTurnPastArticle)
    val scope = rememberCoroutineScope()
    val keys = LocalPageTurnKeys.current
    val background = MaterialTheme.colorScheme.background

    val turn = remember(pages, scope) {
        { direction: PageDirection ->
            scope.launch {
                if (currentPaginate) {
                    val turned = pages.turnPage(direction)

                    if (!turned) {
                        currentOnTurnPastArticle(direction)
                    }
                } else if (direction == PageDirection.FORWARD) {
                    pages.turn(direction, visible = visibleInsets, next = hiddenInsets)
                } else {
                    pages.turn(direction, visible = visibleInsets, next = visibleInsets)
                }

                if (direction == PageDirection.FORWARD) {
                    currentOnHideToolbars()
                }
            }
            Unit
        }
    }

    DisposableEffect(keys, enableKeys, pages) {
        if (!enableKeys) {
            return@DisposableEffect onDispose {}
        }

        val unregister = keys.register(turn)

        onDispose { unregister() }
    }

    LaunchedEffect(paginate, pages.pageTops) {
        if (paginate) {
            pages.alignToPage()
        }
    }

    Box(
        modifier = Modifier
            .pageTapZones(
                enabled = enableTaps,
                onTurn = turn,
                onCenterTap = onToggleToolbars,
            )
            .pageSwipes(
                enabled = paginate,
                onTurn = turn,
            )
    ) {
        Box(
            modifier = Modifier.drawWithContent {
                drawContent()

                val cut = pages.pageCut

                if (paginate && cut != null && cut < size.height) {
                    drawRect(
                        color = background,
                        topLeft = Offset(0f, cut),
                        size = Size(size.width, size.height - cut),
                    )
                }
            }
        ) {
            content()
        }

        if (paginate && (pinToolbars || !showToolbars)) {
            PageFooter(
                title = title,
                page = pages.currentPage + 1,
                count = pages.pageCount,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(PageFooterHeight)
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp),
            )
        }
    }
}

@Composable
private fun PageFooter(
    title: String,
    page: Int,
    count: Int,
    modifier: Modifier = Modifier,
) {
    val style = MaterialTheme.typography.labelMedium
    val color = MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = modifier,
    ) {
        Text(
            text = PageFooterTitle.truncate(title),
            style = style,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.reader_page_indicator, page, count),
            style = style,
            color = color,
        )
    }
}

@Composable
fun rememberPagedInsets(pinToolbars: Boolean): PageInsets {
    val insets = pageInsets(pinToolbars, showToolbars = false)
    val density = LocalDensity.current
    val top = with(density) { PageTopPadding.toPx() }
    val footer = with(density) { PageFooterHeight.toPx() }

    return remember(insets, top, footer) {
        insets.copy(top = insets.top + top, bottom = insets.bottom + footer)
    }
}

private fun Modifier.pageSwipes(
    enabled: Boolean,
    onTurn: (PageDirection) -> Unit,
): Modifier {
    if (!enabled) {
        return this
    }

    return pointerInput(onTurn) {
        var distance = 0f

        detectHorizontalDragGestures(
            onDragStart = { distance = 0f },
            onDragEnd = {
                val threshold = SwipeThreshold.toPx()

                if (distance < -threshold) {
                    onTurn(PageDirection.FORWARD)
                } else if (distance > threshold) {
                    onTurn(PageDirection.BACK)
                }
            },
            onHorizontalDrag = { change, amount ->
                change.consume()
                distance += amount
            },
        )
    }
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

private val PageFooterHeight = 32.dp

private val PageTopPadding = 8.dp

private val SwipeThreshold = 48.dp
