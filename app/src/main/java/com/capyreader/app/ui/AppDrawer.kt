package com.capyreader.app.ui

import androidx.compose.material3.DrawerState
import androidx.compose.runtime.Stable
import androidx.compose.runtime.staticCompositionLocalOf

/**
 * How the article list reaches the feed list. [FeedsDrawerSceneStrategy] provides a [ModalAppDrawer]
 * on medium and wider windows; on compact windows [App] provides a [FeedsPageDrawer] that navigates
 * back to the [Route.Feeds] page instead.
 */
@Stable
interface AppDrawer {
    val isOpen: Boolean

    suspend fun open()

    suspend fun close()
}

@Stable
class ModalAppDrawer(private val state: DrawerState) : AppDrawer {
    override val isOpen: Boolean
        get() = state.isOpen

    override suspend fun open() {
        state.open()
    }

    override suspend fun close() {
        state.close()
    }
}

/** The feed list is a page in the back stack, so "opening" it is navigation and it's never open over the list. */
@Stable
class FeedsPageDrawer(private val navigateToFeeds: () -> Unit) : AppDrawer {
    override val isOpen = false

    override suspend fun open() {
        navigateToFeeds()
    }

    override suspend fun close() = Unit
}

val LocalAppDrawer = staticCompositionLocalOf<AppDrawer?> { null }
