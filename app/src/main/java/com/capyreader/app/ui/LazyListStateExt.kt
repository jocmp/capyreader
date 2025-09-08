package com.capyreader.app.ui

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.paging.compose.LazyPagingItems

/**
 * After recreation, LazyPagingItems first returns 0 items, then the cached items.
 * This behavior/issue resets the LazyListState scroll position.
 * Below is a workaround. More info: https://issuetracker.google.com/issues/177245496.
 *
 * <https://github.com/ReadYouApp/ReadYou/blob/8be88771745dc891cdd1d9229ad668e86dd9532e/app/src/main/java/me/ash/reader/ui/ext/LazyListStateExt.kt#L17-L18>
 */
@Composable
fun <T : Any> LazyPagingItems<T>.rememberLazyListState(): LazyListState {
    return when (itemCount) {
        // Return a different LazyListState instance.
        0 -> remember(this) { LazyListState(0, 0) }
        // Return rememberLazyListState (normal case).
        else -> androidx.compose.foundation.lazy.rememberLazyListState()
    }
}
