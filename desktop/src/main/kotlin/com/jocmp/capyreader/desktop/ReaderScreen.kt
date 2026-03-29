package com.jocmp.capyreader.desktop

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.isCtrlPressed
import androidx.compose.ui.input.key.isMetaPressed
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import java.awt.Desktop
import java.net.URI

private val COMPACT_BREAKPOINT = 700.dp

@Composable
fun ReaderScreen(
    state: ReaderState,
    onSignOut: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val isMac = remember { System.getProperty("os.name").orEmpty().lowercase().contains("mac") }
    val density = LocalDensity.current

    var sidebarWidth by remember { mutableStateOf(240.dp) }
    var sidebarCollapsed by remember { mutableStateOf(false) }
    var listWidth by remember { mutableStateOf(320.dp) }

    val selectedArticle by state.selectedArticle.collectAsDesktopState()

    LaunchedEffect(Unit) {
        state.loadArticles()
        focusRequester.requestFocus()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val containerWidth = maxWidth
        val isCompact by remember(containerWidth) {
            derivedStateOf { containerWidth < COMPACT_BREAKPOINT }
        }
        val showDetail = !isCompact || selectedArticle != null
        val showList = !isCompact || selectedArticle == null

        Row(
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .focusable()
                .onPreviewKeyEvent { event ->
                    if (event.type != KeyEventType.KeyDown) return@onPreviewKeyEvent false
                    val mod = if (isMac) event.isMetaPressed else event.isCtrlPressed

                    when {
                        event.key == Key.J || event.key == Key.DirectionDown -> {
                            state.selectNextArticle(); true
                        }
                        event.key == Key.K || event.key == Key.DirectionUp -> {
                            state.selectPreviousArticle(); true
                        }
                        mod && event.key == Key.R -> {
                            state.refresh(); true
                        }
                        event.key == Key.S -> {
                            state.toggleStar(); true
                        }
                        event.key == Key.M -> {
                            state.toggleRead(); true
                        }
                        mod && event.key == Key.A -> {
                            state.markAllRead(); true
                        }
                        // Cmd/Ctrl+[ or Backspace — back to list in compact mode
                        (mod && event.key == Key.LeftBracket) || (isCompact && event.key == Key.Backspace) -> {
                            state.clearSelection(); true
                        }
                        // Cmd/Ctrl+B — toggle sidebar
                        mod && event.key == Key.B -> {
                            sidebarCollapsed = !sidebarCollapsed; true
                        }
                        event.key == Key.V -> {
                            state.selectedArticle.value?.url?.let {
                                try { Desktop.getDesktop().browse(URI(it.toString())) } catch (_: Exception) {}
                            }
                            true
                        }
                        event.key == Key.Escape -> {
                            if (isCompact && selectedArticle != null) {
                                state.clearSelection()
                            } else {
                                sidebarCollapsed = !sidebarCollapsed
                            }
                            true
                        }
                        else -> false
                    }
                },
        ) {
            AnimatedVisibility(
                visible = !sidebarCollapsed,
                enter = expandHorizontally(),
                exit = shrinkHorizontally(),
            ) {
                Row {
                    FeedSidebar(state = state, width = sidebarWidth, onSignOut = onSignOut)
                    PaneDivider { deltaX ->
                        with(density) {
                            val newWidth = sidebarWidth + deltaX.toDp()
                            sidebarWidth = newWidth.coerceIn(160.dp, 400.dp)
                        }
                    }
                }
            }

            if (showList) {
                ArticleListPane(
                    state = state,
                    width = if (isCompact) containerWidth else listWidth,
                    sidebarCollapsed = sidebarCollapsed,
                    onToggleSidebar = { sidebarCollapsed = !sidebarCollapsed },
                )

                if (!isCompact) {
                    PaneDivider { deltaX ->
                        with(density) {
                            val newWidth = listWidth + deltaX.toDp()
                            listWidth = newWidth.coerceIn(200.dp, 600.dp)
                        }
                    }
                }
            }

            if (showDetail) {
                ArticleDetailPane(
                    state = state,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}
