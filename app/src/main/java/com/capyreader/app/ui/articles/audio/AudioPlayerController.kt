package com.capyreader.app.ui.articles.audio

import android.content.Context
import android.os.Handler
import android.os.Looper
import androidx.annotation.OptIn
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.database.StandaloneDatabaseProvider
import androidx.media3.datasource.cache.CacheDataSource
import androidx.media3.datasource.cache.LeastRecentlyUsedCacheEvictor
import androidx.media3.datasource.cache.SimpleCache
import androidx.media3.datasource.okhttp.OkHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import com.capyreader.app.common.AudioEnclosure
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.OkHttpClient
import java.io.File

private const val CACHE_SIZE_BYTES = 100L * 1024L * 1024L // 100 MB

class AudioPlayerController(
    private val context: Context,
    okHttpClient: OkHttpClient,
) {
    private val cache: SimpleCache
    private val cacheDataSourceFactory: CacheDataSource.Factory

    init {
        val cacheDir = File(context.cacheDir, "audio_cache")
        val databaseProvider = StandaloneDatabaseProvider(context)
        cache = SimpleCache(cacheDir, LeastRecentlyUsedCacheEvictor(CACHE_SIZE_BYTES), databaseProvider)

        val okHttpDataSourceFactory = OkHttpDataSource.Factory(okHttpClient)
        cacheDataSourceFactory = CacheDataSource.Factory()
            .setCache(cache)
            .setUpstreamDataSourceFactory(okHttpDataSourceFactory)
    }
    private var player: ExoPlayer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val positionUpdateRunnable = object : Runnable {
        override fun run() {
            player?.let {
                _currentPosition.value = it.currentPosition
                if (_duration.value == 0L && it.duration > 0) {
                    _duration.value = it.duration
                }
            }
            mainHandler.postDelayed(this, 500)
        }
    }

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentAudio = MutableStateFlow<AudioEnclosure?>(null)
    val currentAudio: StateFlow<AudioEnclosure?> = _currentAudio.asStateFlow()

    @OptIn(UnstableApi::class)
    fun play(audio: AudioEnclosure) {
        mainHandler.post {
            playOnMainThread(audio)
        }
    }

    @OptIn(UnstableApi::class)
    private fun playOnMainThread(audio: AudioEnclosure) {
        val currentUrl = _currentAudio.value?.url

        if (currentUrl == audio.url && player != null) {
            player?.play()
            return
        }

        releaseInternal()

        val mediaSourceFactory = DefaultMediaSourceFactory(context)
            .setDataSourceFactory(cacheDataSourceFactory)

        player = ExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
            .apply {
            val mediaItem = MediaItem.fromUri(audio.url)
            setMediaItem(mediaItem)
            prepare()
            playWhenReady = true

            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    _isPlaying.value = isPlaying
                    if (isPlaying) {
                        startPositionUpdates()
                    } else {
                        stopPositionUpdates()
                    }
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    if (playbackState == Player.STATE_READY) {
                        _duration.value = duration
                    }
                    if (playbackState == Player.STATE_ENDED) {
                        _isPlaying.value = false
                        _currentPosition.value = 0L
                        seekTo(0)
                    }
                }
            })
        }

        _currentAudio.value = audio

        audio.durationSeconds?.let {
            _duration.value = it * 1000
        }
    }

    fun pause() {
        mainHandler.post {
            player?.pause()
        }
    }

    fun resume() {
        mainHandler.post {
            player?.play()
        }
    }

    fun seekTo(positionMs: Long) {
        mainHandler.post {
            player?.seekTo(positionMs)
            _currentPosition.value = positionMs
        }
    }

    fun dismiss() {
        mainHandler.post {
            releaseInternal()
            _currentAudio.value = null
            _isPlaying.value = false
            _currentPosition.value = 0L
            _duration.value = 0L
        }
    }

    private fun releaseInternal() {
        stopPositionUpdates()
        player?.release()
        player = null
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        mainHandler.post(positionUpdateRunnable)
    }

    private fun stopPositionUpdates() {
        mainHandler.removeCallbacks(positionUpdateRunnable)
    }
}
