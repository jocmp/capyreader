package com.capyreader.app.ui.articles.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import kotlin.math.abs

@Stable
internal class ArticleScrollState(
    private val dividerThreshold: Float,
) {
    private val _isScrollingDown = mutableStateOf(false)
    private val _contentOffset = mutableFloatStateOf(0f)

    val isScrollingDown: Boolean by _isScrollingDown

    val showTopDivider: Boolean by derivedStateOf { _contentOffset.floatValue > dividerThreshold }

    val connection = object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            if (abs(available.y) > 2f) {
                _isScrollingDown.value = available.y < 0f
            }
            return Offset.Zero
        }

        override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
            _contentOffset.floatValue -= consumed.y
            if (available.y > 0f) {
                _contentOffset.floatValue = 0f
            }
            return Offset.Zero
        }
    }

    fun reset() {
        _isScrollingDown.value = false
        _contentOffset.floatValue = 0f
    }
}

@Composable
internal fun rememberArticleScrollState(): ArticleScrollState {
    val dividerThreshold = with(LocalDensity.current) { 60.dp.toPx() }

    return remember { ArticleScrollState(dividerThreshold) }
}
