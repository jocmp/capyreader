package com.capyreader.app.ui.articles.audio

internal object SkipCalculator {
    const val SKIP_DURATION_MS = 30_000L

    fun skipBack(currentPosition: Long): Long {
        return maxOf(0L, currentPosition - SKIP_DURATION_MS)
    }

    fun skipForward(currentPosition: Long, duration: Long): Long {
        return if (duration > 0) {
            minOf(duration, currentPosition + SKIP_DURATION_MS)
        } else {
            currentPosition + SKIP_DURATION_MS
        }
    }
}
