package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.input.pointer.PointerEventPass
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
    pinToolbars: Boolean,
    showToolbars: Boolean,
    onHideToolbars: () -> Unit,
    onToggleToolbars: () -> Unit,
    appPreferences: AppPreferences = koinInject(),
    content: @Composable () -> Unit,
) {
    val enableTaps by appPreferences.readerOptions.enablePagingTapGesture.collectChangesWithDefault()
    val enableKeys by appPreferences.readerOptions.enablePageTurnKeys.collectChangesWithDefault()
    val visibleInsets by rememberUpdatedState(pageInsets(pinToolbars, showToolbars))
    val hiddenInsets by rememberUpdatedState(pageInsets(pinToolbars, showToolbars = false))
    val currentOnHideToolbars by rememberUpdatedState(onHideToolbars)
    val scope = rememberCoroutineScope()
    val keys = LocalPageTurnKeys.current

    val turn = remember(pages, scope) {
        { direction: PageDirection ->
            scope.launch {
                if (direction == PageDirection.FORWARD) {
                    pages.turn(direction, visible = visibleInsets, next = hiddenInsets)
                    currentOnHideToolbars()
                } else {
                    pages.turn(direction, visible = visibleInsets, next = visibleInsets)
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

    Box(
        modifier = Modifier.pageTapZones(
            enabled = enableTaps,
            onTurn = turn,
            onCenterTap = onToggleToolbars,
        )
    ) {
        content()
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
            val down = awaitFirstDown(requireUnconsumed = false, pass = PointerEventPass.Final)

            if (down.isConsumed) {
                return@awaitEachGesture
            }

            val up = waitForUpOrCancellation(pass = PointerEventPass.Final)

            if (up == null || up.isConsumed) {
                return@awaitEachGesture
            }

            val isLongPress = up.uptimeMillis - down.uptimeMillis > viewConfiguration.longPressTimeoutMillis

            if (isLongPress) {
                return@awaitEachGesture
            }

            val zone = up.position.x / size.width

            if (zone < EDGE_ZONE) {
                onTurn(PageDirection.BACK)
            } else if (zone > 1f - EDGE_ZONE) {
                onTurn(PageDirection.FORWARD)
            } else {
                onCenterTap()
            }
        }
    }
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

private const val EDGE_ZONE = 1f / 3f
