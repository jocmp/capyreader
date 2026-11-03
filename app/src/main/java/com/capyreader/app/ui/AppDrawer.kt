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
 * or a full page over the article list on compact windows ([FeedsPageState]).
 */
interface NavigationPanel {
    val isOpen: Boolean

    suspend fun open()

    suspend fun close()
}

/**
 * Bridges the app's navigation rail between the window-level host (in [App], which owns the
 * [androidx.compose.material3.ModalWideNavigationRail] so its scrim covers both list and detail
 * panes) and the article list entry, which owns the rail's content and the ViewModel behind it.
 *
 * Only shown on medium and wider windows ([isRailEnabled]): collapsed, it shows
 * [setRailContent]'s items; expanded, the full feed list from [setContent]. Null when no host is
 * present (e.g. previews).
 */
@Stable
class AppDrawerController(
    val state: WideNavigationRailState,
    val isRailEnabled: Boolean,
    val setContent: (content: (@Composable () -> Unit)?) -> Unit,
    val setRailContent: (content: (@Composable () -> Unit)?) -> Unit,
) : NavigationPanel {
    override val isOpen: Boolean
        get() = state.targetValue == WideNavigationRailValue.Expanded

    override suspend fun open() = state.expand()

    override suspend fun close() = state.collapse()
}

/** The compact-window feed list, shown as a full page above the article list. */
@Stable
class FeedsPageState(initiallyOpen: Boolean = false) : NavigationPanel {
    override var isOpen by mutableStateOf(initiallyOpen)
        private set

    override suspend fun open() {
        isOpen = true
    }

    override suspend fun close() {
        isOpen = false
    }

    companion object {
        val Saver = Saver<FeedsPageState, Boolean>(
            save = { it.isOpen },
            restore = { FeedsPageState(initiallyOpen = it) },
        )
    }
}

@Composable
fun rememberFeedsPageState(): FeedsPageState {
    return rememberSaveable(saver = FeedsPageState.Saver) { FeedsPageState() }
}

val LocalAppDrawer = compositionLocalOf<AppDrawerController?> { null }
