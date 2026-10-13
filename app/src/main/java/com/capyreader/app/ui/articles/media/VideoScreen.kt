package com.capyreader.app.ui.articles.media

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.capyreader.app.ui.articles.reader.VideoPlayer
import com.capyreader.app.ui.articles.reader.VideoPlayerViewModel
import org.koin.compose.viewmodel.koinActivityViewModel

@Composable
fun VideoScreen(
    uri: String,
    onDismiss: () -> Unit,
    viewModel: VideoPlayerViewModel = koinActivityViewModel(),
) {
    val activity = LocalActivity.current
    val player = remember(uri) { viewModel.acquire(uri) }

    DisposableEffect(uri) {
        viewModel.fullscreenUri = uri

        onDispose {
            val isChangingConfigurations = activity?.isChangingConfigurations == true

            if (!isChangingConfigurations) {
                viewModel.fullscreenUri = null
            }

            viewModel.release(
                uri = uri,
                isChangingConfigurations = isChangingConfigurations,
            )
        }
    }

    DisposableEffect(activity) {
        val window = activity?.window
        val insets = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
        val wasLightStatusBars = insets?.isAppearanceLightStatusBars == true

        insets?.isAppearanceLightStatusBars = false
        insets?.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insets?.hide(WindowInsetsCompat.Type.systemBars())

        onDispose {
            insets?.show(WindowInsetsCompat.Type.systemBars())
            insets?.isAppearanceLightStatusBars = wasLightStatusBars
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
    ) {
        VideoPlayer(
            player = player,
            isFullscreen = true,
            onToggleFullscreen = onDismiss,
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.displayCutout),
        )
    }
}
