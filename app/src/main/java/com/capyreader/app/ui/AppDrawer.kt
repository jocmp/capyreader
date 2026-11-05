package com.capyreader.app.ui

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * Where the feed list lives: a full page on compact windows ([NavigationPanelState]), or the
 * window-level drawer on medium and wider ([AppDrawerController]).
 */
interface NavigationPanel {
    val isOpen: Boolean

    suspend fun open()

    suspend fun close()
}

/** How the window-level host shows the feed list. */
enum class DrawerMode {
    /** Compact: no window drawer; the list entry shows the feeds as a full page. */
    PAGE,

    /** Medium: a modal drawer opened from the top bar. */
    MODAL,

    /** Expanded and wider: a permanent drawer beside the panes. */
    PERMANENT,
}

/** The compact feeds page's open/closed flag. */
@Stable
class NavigationPanelState(initiallyOpen: Boolean = false) : NavigationPanel {
    override var isOpen by mutableStateOf(initiallyOpen)
        private set

    override suspend fun open() {
        isOpen = true
    }

    override suspend fun close() {
        isOpen = false
    }

    companion object {
        val Saver = Saver<NavigationPanelState, Boolean>(
            save = { it.isOpen },
            restore = { NavigationPanelState(initiallyOpen = it) },
        )
    }
}

@Composable
fun rememberNavigationPanelState(): NavigationPanelState {
    return rememberSaveable(saver = NavigationPanelState.Saver) { NavigationPanelState() }
}

/**
 * Bridges the window-level drawer (in [App], which owns it so it spans both panes) and the article
 * list entry, which owns the drawer's content and the view models behind it.
 *
 * The list entry calls [setContent] to publish its feed list upward. In [DrawerMode.MODAL] it opens
 * and closes the drawer through [state]; a [DrawerMode.PERMANENT] drawer is always shown, so
 * opening and closing it does nothing. Null when no host is present (e.g. previews).
 */
@Stable
class AppDrawerController(
    val state: DrawerState,
    val mode: DrawerMode,
    val setContent: (content: (@Composable () -> Unit)?) -> Unit,
) : NavigationPanel {
    override val isOpen: Boolean
        get() = mode == DrawerMode.MODAL && state.isOpen

    override suspend fun open() {
        if (mode == DrawerMode.MODAL) {
            state.open()
        }
    }

    override suspend fun close() {
        if (mode == DrawerMode.MODAL) {
            state.close()
        }
    }
}

val LocalAppDrawer = compositionLocalOf<AppDrawerController?> { null }
