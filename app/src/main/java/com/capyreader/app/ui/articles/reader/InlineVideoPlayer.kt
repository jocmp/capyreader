package com.capyreader.app.ui.articles.reader

import androidx.annotation.OptIn
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
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
import androidx.media3.ui.compose.state.rememberPlayPauseButtonState
import androidx.media3.ui.compose.state.rememberPresentationState
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
        modifier = modifier
            .aspectRatio(aspectRatio)
            .clickable(
                interactionSource = null,
                indication = null,
            ) {
                showControls = !showControls
            },
    )
}

private fun videoAspectRatio(width: Float?, height: Float?): Float? {
    if (width == null || height == null || width <= 0f || height <= 0f) {
        return null
    }

    return (width / height).coerceAtLeast(MIN_VIDEO_ASPECT_RATIO)
}
