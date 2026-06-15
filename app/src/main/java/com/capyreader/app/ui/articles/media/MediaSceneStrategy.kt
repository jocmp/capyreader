package com.capyreader.app.ui.articles.media

import androidx.compose.runtime.Composable
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.scene.OverlayScene
import androidx.navigation3.scene.Scene
import androidx.navigation3.scene.SceneStrategy
import androidx.navigation3.scene.SceneStrategyScope

/**
 * Renders an entry marked with [overlay] metadata (the [com.capyreader.app.ui.Route.MediaViewer]
 * entry) as an [OverlayScene] drawn on top of the live list/detail panes (its [overlaidEntries]),
 * rather than replacing them. Unlike [androidx.navigation3.scene.DialogScene] the content stays in
 * the same window — no platform Dialog — so it can scale/animate freely over both panes. Being a
 * real back-stack entry, the system back gesture pops it for free.
 */
class MediaSceneStrategy : SceneStrategy<NavKey> {
    override fun SceneStrategyScope<NavKey>.calculateScene(
        entries: List<NavEntry<NavKey>>
    ): Scene<NavKey>? {
        val top = entries.lastOrNull() ?: return null
        if (!top.metadata.containsKey(OVERLAY_KEY)) return null
        // Everything below the viewer keeps rendering (and is laid out by the other strategies).
        val below = entries.dropLast(1)
        if (below.isEmpty()) return null

        return MediaOverlayScene(
            key = top.contentKey,
            entry = top,
            overlaidEntries = below,
        )
    }

    companion object {
        private const val OVERLAY_KEY = "media_overlay"

        /** Marks an entry to be rendered by [MediaSceneStrategy] as a full-window overlay. */
        fun overlay(): Map<String, Any> = mapOf(OVERLAY_KEY to true)
    }
}

private class MediaOverlayScene(
    override val key: Any,
    private val entry: NavEntry<NavKey>,
    override val overlaidEntries: List<NavEntry<NavKey>>,
) : OverlayScene<NavKey> {
    override val entries: List<NavEntry<NavKey>> = listOf(entry)

    override val previousEntries: List<NavEntry<NavKey>> = overlaidEntries

    override val content: @Composable () -> Unit = { entry.Content() }
}
