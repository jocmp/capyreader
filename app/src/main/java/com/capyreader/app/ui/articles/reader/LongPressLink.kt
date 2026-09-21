package com.capyreader.app.ui.articles.reader

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.awaitLongPressOrCancellation
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import com.jocmp.mallet.LinearTextAnnotation

fun Modifier.longPressLink(
    enabled: Boolean,
    linkAt: (Offset) -> LinearTextAnnotation?,
    onLongPress: (LinearTextAnnotation) -> Unit,
): Modifier = composed {
    if (!enabled) {
        return@composed this
    }

    pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            val link = linkAt(down.position) ?: return@awaitEachGesture

            awaitLongPressOrCancellation(down.id) ?: return@awaitEachGesture

            down.consume()
            onLongPress(link)

            var event: PointerEvent
            do {
                event = awaitPointerEvent(PointerEventPass.Initial)
                event.changes.forEach { it.consume() }
            } while (event.changes.any { it.pressed })
        }
    }
}

fun TextLayoutResult?.linkAt(
    position: Offset,
    links: List<LinearTextAnnotation>,
): LinearTextAnnotation? {
    val layout = this ?: return null
    val line = layout.getLineForVerticalPosition(position.y)

    if (position.x < layout.getLineLeft(line) || position.x > layout.getLineRight(line)) {
        return null
    }

    val offset = layout.getOffsetForPosition(position)

    return links.firstOrNull { offset >= it.start && offset <= it.end }
}
