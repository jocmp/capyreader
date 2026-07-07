package com.capyreader.app.ui.articles.audio

import androidx.media3.common.C
import androidx.media3.common.Player
import androidx.media3.common.Timeline

/**
 * Helpers that treat a media3 playlist (multiple [Player] windows) as one
 * continuous timeline. Read-aloud plays each synthesized chunk as its own
 * MediaItem, so position, duration, seeking and ±skip need to span every chunk
 * rather than stopping at a chunk boundary.
 *
 * All functions operate on any [Player] (both the ExoPlayer inside
 * [MediaPlaybackService] and the [androidx.media3.session.MediaController] the
 * app holds), so single-item podcasts behave exactly as before.
 */
object PlaylistTimeline {
    /** Elapsed position across the whole playlist, in ms. */
    fun globalPosition(player: Player): Long {
        val timeline = player.currentTimeline
        if (timeline.isEmpty) return player.currentPosition.coerceAtLeast(0)

        val window = Timeline.Window()
        val priorWindows = (0 until player.currentMediaItemIndex).sumOf { index ->
            timeline.getWindow(index, window)
            if (window.durationMs != C.TIME_UNSET) window.durationMs else 0L
        }
        return priorWindows + player.currentPosition.coerceAtLeast(0)
    }

    /** Total duration of all windows whose duration is known so far, in ms. */
    fun knownDuration(player: Player): Long {
        val timeline = player.currentTimeline
        if (timeline.isEmpty) {
            val duration = player.duration
            return if (duration == C.TIME_UNSET) 0 else duration
        }

        val window = Timeline.Window()
        return (0 until timeline.windowCount).sumOf { index ->
            timeline.getWindow(index, window)
            if (window.durationMs != C.TIME_UNSET) window.durationMs else 0L
        }
    }

    /** Seek to a global position, mapping it onto the correct window + offset. */
    fun seekToGlobal(player: Player, globalMs: Long) {
        val timeline = player.currentTimeline
        if (timeline.isEmpty) {
            player.seekTo(globalMs.coerceAtLeast(0))
            return
        }

        val window = Timeline.Window()
        var remaining = globalMs.coerceAtLeast(0)
        val lastIndex = timeline.windowCount - 1
        for (i in 0..lastIndex) {
            timeline.getWindow(i, window)
            val duration = window.durationMs
            if (duration == C.TIME_UNSET || remaining < duration || i == lastIndex) {
                val offset =
                    if (duration == C.TIME_UNSET) remaining else remaining.coerceAtMost(duration)
                player.seekTo(i, offset)
                return
            }
            remaining -= duration
        }
    }

    /** Skip by [deltaMs] across the whole timeline, clamped to the known range. */
    fun skip(player: Player, deltaMs: Long) {
        val known = knownDuration(player).coerceAtLeast(0)
        val target = (globalPosition(player) + deltaMs).coerceIn(0, known)
        seekToGlobal(player, target)
    }
}
