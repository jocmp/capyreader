package com.capyreader.app.ui.articles

import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.PaneExpansionAnchor
import androidx.compose.material3.adaptive.layout.PaneExpansionState
import androidx.compose.material3.adaptive.layout.rememberPaneExpansionState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.isCompact
import com.capyreader.app.ui.isLarge
import com.jocmp.capy.logging.CapyLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.koin.compose.koinInject

private val DetailFullscreenAnchor = PaneExpansionAnchor.Proportion(0f)

const val DefaultPaneExpansionIndex = 3

private fun articlePaneAnchors(isLarge: Boolean): List<PaneExpansionAnchor> = buildList {
    add(PaneExpansionAnchor.Proportion(0f))
    val minProportion = if (isLarge) 25 else 35
    (minProportion..70 step 5).forEach { add(PaneExpansionAnchor.Proportion(it / 100f)) }
    add(PaneExpansionAnchor.Proportion(1f))
}

@Stable
class ArticlePaneExpansion(
    val state: PaneExpansionState,
    val isFullscreen: Boolean,
    private val anchors: List<PaneExpansionAnchor>,
    private val lastAnchorIndex: Int,
    private val scope: CoroutineScope,
) {
    private val isListFullscreen get() = lastAnchorIndex == anchors.lastIndex

    fun toggleFullscreen() {
        scope.launch {
            if (isFullscreen) {
                state.animateTo(anchors[lastAnchorIndex])
            } else {
                state.animateTo(DetailFullscreenAnchor)
            }
        }
    }

    suspend fun restore() {
        if (isListFullscreen) {
            state.animateTo(DetailFullscreenAnchor)
        } else {
            state.animateTo(anchors[lastAnchorIndex])
        }
    }

    fun reset() {
        scope.launch {
            if (isFullscreen) {
                state.animateTo(anchors[lastAnchorIndex])
            }
        }
    }
}

@OptIn(ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun rememberArticlePaneExpansion(
    appPreferences: AppPreferences = koinInject(),
): ArticlePaneExpansion {
    val anchors = articlePaneAnchors(isLarge = isLarge())

    val savedIndex = appPreferences.paneExpansionIndex.get()
        .coerceIn(0, anchors.lastIndex)

    val paneExpansionState = rememberPaneExpansionState(
        anchors = anchors,
        initialAnchoredIndex = savedIndex,
    )
    val compact = isCompact()
    val scope = rememberCoroutineScope()
    var lastAnchorIndex by rememberSaveable { mutableIntStateOf(savedIndex) }
    val listFullscreenIndex = anchors.lastIndex

    LaunchedEffect(paneExpansionState.currentAnchor) {
        val anchor = paneExpansionState.currentAnchor ?: return@LaunchedEffect
        val index = anchors.indexOf(anchor)

        if (index in 1..listFullscreenIndex) {
            lastAnchorIndex = index
            appPreferences.paneExpansionIndex.set(index)
            CapyLog.debug("scaffold_save_anchor", mapOf("index" to index))
        }
    }

    val isFullscreen = !compact &&
            paneExpansionState.currentAnchor == DetailFullscreenAnchor

    return remember(paneExpansionState, isFullscreen, lastAnchorIndex, anchors, scope) {
        ArticlePaneExpansion(
            state = paneExpansionState,
            isFullscreen = isFullscreen,
            anchors = anchors,
            lastAnchorIndex = lastAnchorIndex,
            scope = scope,
        )
    }
}
