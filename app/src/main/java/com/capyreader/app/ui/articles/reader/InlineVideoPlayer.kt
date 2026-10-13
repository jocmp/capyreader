package com.capyreader.app.ui.articles.reader

import androidx.activity.compose.LocalActivity
import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.media3.common.util.ExperimentalApi
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.compose.material3.Player
import androidx.media3.ui.compose.material3.PlayerDefaults
import androidx.media3.ui.compose.material3.indicator.DurationText
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPresentationState
import com.capyreader.app.R
import com.capyreader.app.ui.articles.media.MediaColors
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay
import org.koin.compose.viewmodel.koinActivityViewModel

private val CONTROLS_TIMEOUT = 3.seconds

private val VideoColorScheme = darkColorScheme(
    background = Color.Black,
    surface = Color.Black,
)

@OptIn(UnstableApi::class)
@Composable
fun InlineVideoPlayer(
    uri: String,
    initialAspectRatio: Float,
    onFullscreen: (uri: String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: VideoPlayerViewModel = koinActivityViewModel(),
) {
    val activity = LocalActivity.current
    val player = remember(uri) { viewModel.acquire(uri) }

    DisposableEffect(uri) {
        onDispose {
            viewModel.release(
                uri = uri,
                isChangingConfigurations = activity?.isChangingConfigurations == true,
            )
        }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE, lifecycleOwner = ProcessLifecycleOwner.get()) {
        player.pause()
    }

    val videoSize = rememberPresentationState(player).videoSizeDp
    val aspectRatio = videoAspectRatio(videoSize?.width, videoSize?.height) ?: initialAspectRatio
    val frame = modifier.aspectRatio(aspectRatio)

    if (viewModel.fullscreenUri == uri) {
        Box(modifier = frame)
        return
    }

    VideoPlayer(
        player = player,
        isFullscreen = false,
        onToggleFullscreen = { onFullscreen(uri) },
        modifier = frame,
    )
}

@OptIn(UnstableApi::class, ExperimentalApi::class)
@Composable
fun VideoPlayer(
    player: ExoPlayer,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPaused = rememberPlayPauseButtonState(player).showPlay
    var showControls by remember(player) { mutableStateOf(false) }

    LaunchedEffect(showControls, isPaused) {
        if (showControls && !isPaused) {
            delay(CONTROLS_TIMEOUT)
            showControls = false
        }
    }

    MaterialTheme(colorScheme = VideoColorScheme) {
        CompositionLocalProvider(LocalContentColor provides MediaColors.buttonContentColor) {
            Player(
                player = player,
                showControls = showControls || isPaused,
                centerControls = { player, visible ->
                    PlayerDefaults.CenterControls(
                        player = player,
                        visible = visible,
                        modifier = Modifier.fillMaxWidth(),
                        backSecondary = {},
                        forwardSecondary = {},
                    )
                },
                bottomControls = { player, visible ->
                    PlayerDefaults.BottomControls(
                        player = player,
                        visible = visible,
                        right = { player ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                DurationText(
                                    player = player,
                                    modifier = Modifier.padding(start = 8.dp),
                                )
                                FullscreenButton(
                                    isFullscreen = isFullscreen,
                                    onClick = onToggleFullscreen,
                                )
                            }
                        },
                    )
                },
                modifier = modifier
                    .clickable(
                        interactionSource = null,
                        indication = null,
                    ) {
                        showControls = !showControls
                    },
            )
        }
    }
}

@Composable
private fun FullscreenButton(
    isFullscreen: Boolean,
    onClick: () -> Unit,
) {
    val icon = if (isFullscreen) {
        Icons.Filled.FullscreenExit
    } else {
        Icons.Filled.Fullscreen
    }

    val label = if (isFullscreen) {
        stringResource(R.string.reader_video_exit_fullscreen)
    } else {
        stringResource(R.string.reader_video_enter_fullscreen)
    }

    IconButton(onClick = onClick) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MediaColors.buttonContentColor,
        )
    }
}
