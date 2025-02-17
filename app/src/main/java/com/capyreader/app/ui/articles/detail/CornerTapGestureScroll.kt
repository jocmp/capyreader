package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.scrollBy
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalHapticFeedback
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.collectChangesWithDefault
import com.jocmp.capy.common.launchUI
import org.koin.compose.koinInject

@Composable
fun CornerTapGestureScroll(
    maxArticleHeight: Float,
    scrollState: ScrollState,
    appPreferences: AppPreferences = koinInject(),
    content: @Composable () -> Unit,
) {
    val enabled by appPreferences.readerOptions.enablePagingTapGesture.collectChangesWithDefault()

    if (!enabled) {
        return content()
    }

    var columnHeightPx by remember { mutableFloatStateOf(0f) }
    val jumpHeight by remember { derivedStateOf { columnHeightPx * JUMP_PROPORTION } }

    val back = back(jumpHeight = jumpHeight)
    val forward = forward(maxHeight = maxArticleHeight, jumpHeight = jumpHeight)

    Box(
        Modifier
            .onGloballyPositioned { coordinates ->
                columnHeightPx = coordinates.size.height.toFloat()
            }
    ) {
        content()
        ReaderPagingButton(direction = back, scrollState)
        ReaderPagingButton(direction = forward, scrollState)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BoxScope.ReaderPagingButton(
    direction: ReaderPagingButtonModel,
    scrollState: ScrollState,
) {
    val scope = rememberCoroutineScope()
    val haptics = LocalHapticFeedback.current

    val performHaptic = {
        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth(0.33f)
            .fillMaxHeight(0.33f)
            .align(direction.alignment)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {
                    scope.launchUI {
                        scrollState.scrollBy(direction.scrollBy)
                    }
                },
                onLongClick = {
                    performHaptic()
                    scope.launchUI {
                        scrollState.scrollTo(direction.scrollTo)
                    }
                },
            )
    )
}

private data class ReaderPagingButtonModel(
    val scrollTo: Int,
    val scrollBy: Float,
    val alignment: Alignment,
)

private fun back(jumpHeight: Float) = ReaderPagingButtonModel(
    scrollBy = -jumpHeight,
    scrollTo = 0,
    alignment = Alignment.BottomStart
)

private fun forward(
    maxHeight: Float,
    jumpHeight: Float,
) = ReaderPagingButtonModel(
    scrollBy = jumpHeight,
    scrollTo = maxHeight.toInt(),
    alignment = Alignment.BottomEnd
)

private const val JUMP_PROPORTION = 0.98f
