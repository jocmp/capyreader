package com.capyreader.app.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope
import kotlinx.coroutines.launch

/**
 * On medium and wider windows, hoists the [Route.Feeds] root entry into a modal drawer that wraps
 * the article panes, so the back stack keeps the same `[Feeds, ArticleList, ArticleDetail?]` shape
 * at every window size and only its presentation changes.
 *
 * The rest of the stack is laid out by [innerStrategies] (list-detail, then single pane) and the
 * resulting scene is drawn inside the drawer. Back from the list never pops to [Route.Feeds] here:
 * when the inner scene has nowhere to go back to, neither does this one, so the list's own back
 * handling (open drawer, parent filter, exit) applies as before.
 *
 * Only scenes topped by an entry marked [drawerPane] are wrapped; Settings, the video viewer, and
 * a lone [Route.Feeds] (e.g. unfolding while on the compact feeds page) fall through to the other
 * strategies.
 */
class FeedsDrawerSceneStrategy(
    private val drawerState: DrawerState,
    private val enabled: Boolean,
    private val innerStrategies: List<SceneStrategy<NavKey>>,
) : SceneStrategy<NavKey> {
    override fun SceneStrategyScope<NavKey>.calculateScene(
        entries: List<NavEntry<NavKey>>
    ): Scene<NavKey>? {
        if (!enabled) return null

        val feeds = entries.firstOrNull()?.takeIf { it.metadata.containsKey(FEEDS_KEY) }
            ?: return null
        val panes = entries.drop(1)
        if (panes.lastOrNull()?.metadata?.containsKey(PANE_KEY) != true) return null

        val inner = innerStrategies.firstNotNullOfOrNull { strategy ->
            with(strategy) { calculateScene(panes) }
        } ?: return null

        return FeedsDrawerScene(feeds = feeds, inner = inner, drawerState = drawerState)
    }

    companion object {
        private const val FEEDS_KEY = "feeds_drawer_content"
        private const val PANE_KEY = "feeds_drawer_pane"

        /** Marks the entry rendered as the drawer's content. Must be the root of the back stack. */
        fun feeds(): Map<String, Any> = mapOf(FEEDS_KEY to true)

        /** Marks an entry whose scene sits inside the drawer. */
        fun drawerPane(): Map<String, Any> = mapOf(PANE_KEY to true)
    }
}

private class FeedsDrawerScene(
    private val feeds: NavEntry<NavKey>,
    private val inner: Scene<NavKey>,
    private val drawerState: DrawerState,
) : Scene<NavKey> {
    override val key: Any = inner::class to inner.key

    override val entries: List<NavEntry<NavKey>> = listOf(feeds) + inner.entries

    override val previousEntries: List<NavEntry<NavKey>> =
        inner.previousEntries.takeIf { it.isNotEmpty() }?.let { listOf(feeds) + it }.orEmpty()

    override val metadata: Map<String, Any>
        get() = inner.metadata

    override val content: @Composable () -> Unit = {
        val scope = rememberCoroutineScope()
        val drawer = remember(drawerState) { ModalAppDrawer(drawerState) }

        BackHandler(drawerState.isOpen) {
            scope.launch { drawerState.close() }
        }

        CompositionLocalProvider(LocalAppDrawer provides drawer) {
            ModalNavigationDrawer(
                drawerState = drawerState,
                gesturesEnabled = drawerState.isOpen,
                drawerContent = {
                    ModalDrawerSheet(windowInsets = WindowInsets(0)) {
                        feeds.Content()
                    }
                },
            ) {
                inner.content()
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is FeedsDrawerScene) return false

        return feeds == other.feeds && inner == other.inner && drawerState == other.drawerState
    }

    override fun hashCode(): Int {
        return (feeds.hashCode() * 31 + inner.hashCode()) * 31 + drawerState.hashCode()
    }
}
