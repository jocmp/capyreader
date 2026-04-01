package com.capyreader.app.ui.articles.detail

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

@Composable
internal fun ImmersiveMode(enabled: Boolean) {
    val view = LocalView.current
    val window = (view.context as? Activity)?.window ?: return

    val controller = WindowCompat.getInsetsController(window, view)

    LaunchedEffect(enabled) {
        if (enabled) {
            controller.systemBarsBehavior = BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        } else {
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }
    }
}
