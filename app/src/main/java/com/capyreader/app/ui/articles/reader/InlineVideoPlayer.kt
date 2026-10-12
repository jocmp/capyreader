package com.capyreader.app.ui.articles.reader

import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.ExperimentalApi
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.compose.material3.Player
import androidx.media3.ui.compose.material3.PlayerDefaults
import androidx.media3.ui.compose.material3.indicator.DurationText
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPresentationState
import com.capyreader.app.R
import com.jocmp.capy.accounts.baseHttpClient
import kotlin.time.Duration.Companion.seconds
import kotlinx.coroutines.delay

private const val MIN_VIDEO_ASPECT_RATIO = 3f / 4f

private val CONTROLS_TIMEOUT = 3.seconds

@OptIn(UnstableApi::class, ExperimentalApi::class)
@Composable
fun InlineVideoPlayer(
    uri: String,
    initialAspectRatio: Float,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    val player = remember(uri) {
        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(OkHttpDataSource.Factory(baseHttpClient()))

        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .setUsage(C.USAGE_MEDIA)
            .build()

        ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(uri))
                playWhenReady = true
                prepare()
            }
    }

    DisposableEffect(player) {
        onDispose { player.release() }
    }

    LifecycleEventEffect(Lifecycle.Event.ON_PAUSE) {
        player.pause()
    }

    val videoSize = rememberPresentationState(player).videoSizeDp
    val aspectRatio = videoAspectRatio(videoSize?.width, videoSize?.height) ?: initialAspectRatio

    var isFullscreen by rememberSaveable(uri) { mutableStateOf(false) }

    if (isFullscreen) {
        Box(modifier = modifier.aspectRatio(aspectRatio))

        FullscreenVideoDialog(onDismissRequest = { isFullscreen = false }) {
            VideoPlayer(
                player = player,
                isFullscreen = true,
                onToggleFullscreen = { isFullscreen = false },
                modifier = Modifier.fillMaxSize(),
            )
        }
    } else {
        VideoPlayer(
            player = player,
            isFullscreen = false,
            onToggleFullscreen = { isFullscreen = true },
            modifier = modifier.aspectRatio(aspectRatio),
        )
    }
}

@OptIn(UnstableApi::class, ExperimentalApi::class)
@Composable
private fun VideoPlayer(
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
            tint = Color.White,
        )
    }
}

fun videoAspectRatio(width: Float?, height: Float?): Float? {
    if (width == null || height == null || width <= 0f || height <= 0f) {
        return null
    }

    return (width / height).coerceAtLeast(MIN_VIDEO_ASPECT_RATIO)
}
