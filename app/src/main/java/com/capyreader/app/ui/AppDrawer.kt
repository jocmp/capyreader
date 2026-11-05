package com.capyreader.app.ui

import androidx.compose.material3.WideNavigationRailState
import androidx.compose.material3.WideNavigationRailValue
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue

/**
 * Where the feed list lives: the expanded rail on medium and wider windows ([AppDrawerController]),
 * or a full page over the article list on compact windows ([NavigationPanelState]).
 */
interface NavigationPanel {
    val isOpen: Boolean

    suspend fun open()

    suspend fun close()
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
 * Bridges the Material [androidx.compose.material3.WideNavigationRail] between the window-level
 * host (in [App], which lays it out beside both panes) and the article list entry, which owns the
 * rail's items and the view models behind them.
 *
 * Only shown on medium and wider windows ([isRailEnabled]). The list entry publishes its items
 * through [setRailContent]; they're told whether the rail is expanded so each
 * [androidx.compose.material3.WideNavigationRailItem] can animate its label. Null when no host is
 * present (e.g. previews).
 */
@Stable
class AppDrawerController(
    val state: WideNavigationRailState,
    val isRailEnabled: Boolean,
    val setRailContent: (content: (@Composable (railExpanded: Boolean) -> Unit)?) -> Unit,
) : NavigationPanel {
    override val isOpen: Boolean
        get() = state.targetValue == WideNavigationRailValue.Expanded

    override suspend fun open() = state.expand()

    override suspend fun close() = state.collapse()
}

val LocalAppDrawer = compositionLocalOf<AppDrawerController?> { null }
