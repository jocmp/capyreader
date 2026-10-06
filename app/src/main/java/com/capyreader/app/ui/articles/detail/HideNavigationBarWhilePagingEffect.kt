package com.capyreader.app.ui.articles.detail

import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.collectChangesWithCurrent
import org.koin.compose.koinInject

@Composable
fun HideNavigationBarWhilePagingEffect(
    hide: Boolean,
    appPreferences: AppPreferences = koinInject(),
) {
    val enableTaps by appPreferences.readerOptions.enablePagingTapGesture.collectChangesWithCurrent()
    val enableKeys by appPreferences.readerOptions.enablePageTurnKeys.collectChangesWithCurrent()
    val window = LocalActivity.current?.window ?: return
    val hideNavigationBar = hide && (enableTaps || enableKeys)

    DisposableEffect(window, hideNavigationBar) {
        val controller = WindowCompat.getInsetsController(window, window.decorView)

        if (hideNavigationBar) {
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.navigationBars())
        }

        onDispose {
            controller.show(WindowInsetsCompat.Type.navigationBars())
        }
    }
}
