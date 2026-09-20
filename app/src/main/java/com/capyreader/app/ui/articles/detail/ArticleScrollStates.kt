package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember

private const val MAX_RETAINED = 6

@Composable
fun rememberArticleScrollStates(): ArticleScrollStates {
    return remember { ArticleScrollStates() }
}

@Stable
class ArticleScrollStates {
    private val states = mutableMapOf<String, ScrollState>()
    private val recentlyUsed = ArrayDeque<String>()

    fun scrollState(articleID: String): ScrollState {
        recentlyUsed.remove(articleID)
        recentlyUsed.addLast(articleID)

        removeOldestEntries()

        return states.getOrPut(articleID) { ScrollState(initial = 0) }
    }

    private fun removeOldestEntries() {
        while (recentlyUsed.size > MAX_RETAINED) {
            states.remove(recentlyUsed.removeFirst())
        }
    }
}
