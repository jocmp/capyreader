package com.capyreader.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import kotlin.math.absoluteValue
import kotlin.math.withSign

@Composable
fun rememberSwiperState(
    onStart: () -> Unit = {},
    onDismiss: () -> Unit = {},
    onEnd: () -> Unit = {},
): SwiperState {
    return rememberSaveable(
        saver = SwiperState.Saver(
            onStart, onDismiss, onEnd
        )
    ) {
        SwiperState(
            onStart = onStart,
            onDismiss = onDismiss,
            onEnd = onEnd,
        )
    }
}

@Stable
class SwiperState(
    internal val onStart: () -> Unit = {},
    internal val onDismiss: () -> Unit = {},
    internal val onEnd: () -> Unit = {},
    initialOffset: Float = 0f,
) {
    internal var maxHeight: Int = 0
        set(value) {
            field = value
            _offset.updateBounds(lowerBound = -value.toFloat(), upperBound = value.toFloat())
        }
    internal var dismissed by mutableStateOf(false)
    private var _offset = Animatable(initialOffset)

    val offset: Float
        get() = _offset.value

    val progress: Float
        get() = (offset.absoluteValue / (if (maxHeight == 0) 1 else maxHeight)).coerceIn(
            maximumValue = 1f,
            minimumValue = 0f
        )

    internal suspend fun snap(value: Float) {
        _offset.snapTo(value)
    }

    internal suspend fun fling(velocity: Float) {
        val value = _offset.value
        when {
            velocity.absoluteValue > 4000f -> {
                dismiss(velocity)
            }
            value.absoluteValue < maxHeight * 0.5 -> {
                restore()
            }
            value.absoluteValue < maxHeight -> {
                dismiss(velocity)
            }
        }
    }

    private suspend fun dismiss(velocity: Float) {
        dismissed = true
        _offset.animateTo(maxHeight.toFloat().withSign(_offset.value), initialVelocity = velocity)
        onDismiss.invoke()
    }

    private suspend fun restore() {
        onEnd.invoke()
        _offset.animateTo(0f)
    }

    companion object {
        fun Saver(
            onStart: () -> Unit = {},
            onDismiss: () -> Unit = {},
            onEnd: () -> Unit = {},
        ): Saver<SwiperState, *> = listSaver(
            save = {
                listOf(
                    it.offset,
                )
            },
            restore = {
                SwiperState(
                    onStart = onStart,
                    onDismiss = onDismiss,
                    onEnd = onEnd,
                    initialOffset = it[0],
                )
            }
        )
    }
}
