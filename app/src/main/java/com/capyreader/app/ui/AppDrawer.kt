package com.capyreader.app.ui

import androidx.compose.material3.WideNavigationRailState
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.material3.rememberWideNavigationRailState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember

/**
 * Bridges the app's navigation rail between the window-level host (in [App], which owns the
 * [androidx.compose.material3.ModalWideNavigationRail] so its scrim covers both list and detail
 * panes) and the article list entry, which owns the rail's content and the ViewModel behind it.
 *
 * The rail expands into the full feed list ([setContent]). On medium and wider windows
 * ([isRailEnabled]) it also stays on screen collapsed, showing [setRailContent]'s items; on compact
 * windows it's hidden until opened from the top bar. Null when no host is present (e.g. previews).
 */
@Stable
class AppDrawerController(
    val state: WideNavigationRailState,
    val isRailEnabled: Boolean,
    val setContent: (content: (@Composable () -> Unit)?) -> Unit,
    val setRailContent: (content: (@Composable () -> Unit)?) -> Unit,
) {
    val isOpen: Boolean
        get() = state.targetValue == WideNavigationRailValue.Expanded

    suspend fun open() = state.expand()

    suspend fun close() = state.collapse()
}

/** A host-less controller for previews, where there's no window-level rail to drive. */
@Composable
fun rememberDetachedAppDrawer(): AppDrawerController {
    val state = rememberWideNavigationRailState()

    return remember(state) {
        AppDrawerController(
            state = state,
            isRailEnabled = false,
            setContent = {},
            setRailContent = {},
        )
    }
}

val LocalAppDrawer = compositionLocalOf<AppDrawerController?> { null }
