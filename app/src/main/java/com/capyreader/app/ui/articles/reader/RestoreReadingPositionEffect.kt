package com.capyreader.app.ui.articles.reader

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.flow.takeWhile

private class SavedReadingPosition(val position: ReadingPosition?)

@Composable
fun RestoreReadingPositionEffect(
    articleID: String,
    anchors: AnchorRegistry,
    scrollState: ScrollState,
) {
    val saved = rememberSaveable(
        articleID,
        saver = readingPositionSaver(anchors),
    ) {
        SavedReadingPosition(position = null)
    }

    LaunchedEffect(saved) {
        val position = saved.position ?: return@LaunchedEffect

        snapshotFlow { scrollState.maxValue }
            .takeWhile { !scrollState.isScrollInProgress }
            .collect { anchors.restore(position) }
    }
}

private fun readingPositionSaver(anchors: AnchorRegistry) =
    Saver<SavedReadingPosition, List<Float>>(
        save = {
            anchors.readingPosition()?.let { listOf(it.index.toFloat(), it.fraction) }
        },
        restore = { (index, fraction) ->
            SavedReadingPosition(ReadingPosition(index = index.toInt(), fraction = fraction))
        },
    )
