package com.capyreader.app.ui.articles.reader

import android.app.Application
import androidx.annotation.OptIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer

class VideoPlayerViewModel(
    private val application: Application,
) : ViewModel() {
    private val players = mutableMapOf<String, ExoPlayer>()

    var fullscreenUri by mutableStateOf<String?>(null)

    fun player(uri: String): ExoPlayer {
        return players.getOrPut(uri) { buildPlayer(uri) }
    }

    fun release(uri: String) {
        players.remove(uri)?.release()

        if (fullscreenUri == uri) {
            fullscreenUri = null
        }
    }

    override fun onCleared() {
        players.values.forEach { it.release() }
        players.clear()
    }

    @OptIn(UnstableApi::class)
    private fun buildPlayer(uri: String): ExoPlayer {
        val audioAttributes = AudioAttributes.Builder()
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .setUsage(C.USAGE_MEDIA)
            .build()

        return ExoPlayer.Builder(application)
            .setMediaSourceFactory(videoMediaSourceFactory(application))
            .setAudioAttributes(audioAttributes, true)
            .setHandleAudioBecomingNoisy(true)
            .build()
            .apply {
                setMediaItem(MediaItem.fromUri(uri))
                playWhenReady = true
                prepare()
            }
    }
}
