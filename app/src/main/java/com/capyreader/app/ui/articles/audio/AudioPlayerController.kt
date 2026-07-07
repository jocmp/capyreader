package com.capyreader.app.ui.articles.audio

import android.content.ComponentName
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.net.Uri
import android.os.Looper
import androidx.annotation.OptIn
import androidx.core.content.ContextCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.capyreader.app.R
import com.capyreader.app.common.AudioEnclosure
import com.google.common.util.concurrent.ListenableFuture
import com.jocmp.capy.logging.CapyLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import kotlin.coroutines.resume

class AudioPlayerController(
    private val context: Context,
) {
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var mediaController: MediaController? = null
    private var positionUpdateJob: Job? = null
    private var synthJob: Job? = null
    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val synthesizer by lazy { ArticleTextToSpeech(context) }

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    /** Non-null while the current media session is reading an article aloud (TTS). */
    private val _currentReadAloudArticleId = MutableStateFlow<String?>(null)
    val currentReadAloudArticleId: StateFlow<String?> = _currentReadAloudArticleId.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _currentAudio = MutableStateFlow<AudioEnclosure?>(null)
    val currentAudio: StateFlow<AudioEnclosure?> = _currentAudio.asStateFlow()

    private fun ensureController(onReady: (MediaController) -> Unit) {
        mediaController?.let {
            if (it.isConnected) {
                onReady(it)
                return
            }
        }

        val sessionToken = SessionToken(
            context,
            ComponentName(context, MediaPlaybackService::class.java)
        )

        controllerFuture = MediaController.Builder(context, sessionToken)
            .setApplicationLooper(Looper.getMainLooper())
            .buildAsync()
        controllerFuture?.addListener({
            try {
                val controller = controllerFuture?.get()
                mediaController = controller
                controller?.let {
                    setupPlayerListener(it)
                    onReady(it)
                }
            } catch (e: Exception) {
                CapyLog.error("audio_player", e)
            }
        }, ContextCompat.getMainExecutor(context))
    }

    private suspend fun awaitController(): MediaController? =
        suspendCancellableCoroutine { continuation ->
            ensureController { controller ->
                if (continuation.isActive) {
                    continuation.resume(controller)
                }
            }
        }

    private fun setupPlayerListener(controller: MediaController) {
        controller.addListener(object : Player.Listener {
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
                    val known = PlaylistTimeline.knownDuration(controller)
                    if (known > 0) {
                        _duration.value = known
                    }
                }
                if (playbackState == Player.STATE_ENDED) {
                    _isPlaying.value = false
                    controller.pause()
                }
            }
        })
    }

    @OptIn(UnstableApi::class)
    fun play(audio: AudioEnclosure) {
        mainScope.launch {
            synthJob?.cancel()
            _currentReadAloudArticleId.value = null
            val currentUrl = _currentAudio.value?.url

            if (currentUrl == audio.url && mediaController?.isConnected == true) {
                mediaController?.play()
                return@launch
            }

            ensureController { controller ->
                val mediaItem = MediaItem.Builder()
                    .setUri(audio.url)
                    .setMediaMetadata(
                        MediaMetadata.Builder()
                            .setTitle(audio.title)
                            .setArtist(audio.feedName)
                            .setArtworkUri(audio.artworkUrl?.let { Uri.parse(it) })
                            .build()
                    )
                    .build()

                controller.setMediaItem(mediaItem)
                controller.prepare()
                controller.playWhenReady = true

                _currentAudio.value = audio
                audio.durationSeconds?.let {
                    _duration.value = it * 1000
                }
            }
        }
    }

    /**
     * Read an article aloud through the shared media session, loading progressively:
     * chunk 0 is synthesized and playback starts immediately, then the remaining
     * chunks render in the background and are appended to the playlist as they land
     * (so playback begins without waiting for the whole article and the seek range
     * grows over time). Playback runs through the media3 player, so it continues
     * with the screen off and exposes notification/lock-screen controls. Always
     * (re)starts from the beginning — call again with a new [speed]/[pitch] to
     * restart at a different rate.
     */
    @OptIn(UnstableApi::class)
    fun readAloud(
        articleID: String,
        title: String,
        feedName: String,
        artworkUrl: String?,
        contentHTML: String,
        speed: Float,
        pitch: Float,
    ) {
        synthJob?.cancel()
        synthJob = mainScope.launch {
            clearMedia()

            val chunks = ArticleTextToSpeech.chunk(ArticleTextToSpeech.plainText(contentHTML))
            if (chunks.isEmpty()) return@launch

            _currentReadAloudArticleId.value = articleID
            _currentAudio.value = AudioEnclosure(
                url = "tts:$articleID",
                title = title,
                feedName = feedName,
                durationSeconds = null,
                artworkUrl = artworkUrl,
            )
            // Surface as active while the first chunk synthesizes.
            _isPlaying.value = true

            val outputDir = ttsCacheDir().apply {
                deleteRecursively()
                mkdirs()
            }

            val ready = withContext(Dispatchers.IO) { synthesizer.begin(speed, pitch) }
            if (!isActive || _currentReadAloudArticleId.value != articleID) return@launch
            if (!ready) {
                dismiss()
                return@launch
            }

            val controller = awaitController()
            if (controller == null || !isActive || _currentReadAloudArticleId.value != articleID) {
                return@launch
            }

            var started = false
            chunks.forEachIndexed { index, chunk ->
                if (!isActive || _currentReadAloudArticleId.value != articleID) return@launch

                val file = withContext(Dispatchers.IO) {
                    synthesizer.synthesize(chunk, index, outputDir)
                } ?: return@forEachIndexed

                if (!isActive || _currentReadAloudArticleId.value != articleID) return@launch

                controller.addMediaItem(mediaItemFor(file, title, feedName, artworkUrl))

                if (!started) {
                    controller.prepare()
                    controller.playWhenReady = true
                    started = true
                }
            }

            if (!started) {
                dismiss()
            }
        }
    }

    private fun mediaItemFor(
        file: File,
        title: String,
        feedName: String,
        artworkUrl: String?,
    ): MediaItem {
        val metadata = MediaMetadata.Builder()
            .setTitle(title)
            .setArtist(feedName)

        if (artworkUrl != null) {
            metadata.setArtworkUri(Uri.parse(artworkUrl))
        } else {
            // Articles without an image would otherwise fall back to the system
            // default media-notification accent (blue), which renders as a dark,
            // low-contrast gray on e-ink. Supply the capy on the app's warm
            // launcher-icon background so the notification stays high-contrast.
            defaultArtwork?.let { metadata.setArtworkData(it, MediaMetadata.PICTURE_TYPE_FRONT_COVER) }
        }

        return MediaItem.Builder()
            .setUri(Uri.fromFile(file))
            .setMediaMetadata(metadata.build())
            .build()
    }

    /** The capy on the app's warm launcher-icon background (#E2D4B0), as PNG bytes. */
    private val defaultArtwork: ByteArray? by lazy { buildDefaultArtwork() }

    private fun buildDefaultArtwork(): ByteArray? = try {
        val size = 512
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.parseColor("#E2D4B0"))
        ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground)?.let { capy ->
            val inset = (size * 0.14f).toInt()
            capy.setBounds(inset, inset, size - inset, size - inset)
            capy.draw(canvas)
        }
        ByteArrayOutputStream().use { stream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.toByteArray()
        }
    } catch (e: Exception) {
        CapyLog.error("read_aloud_artwork", e)
        null
    }

    /** Stop reading aloud if the active session is a TTS read (leaves podcasts alone). */
    fun stopReadAloud() {
        if (_currentReadAloudArticleId.value != null) {
            dismiss()
        }
    }

    /** Stop an active read-aloud unless it belongs to [articleID] (used on article change). */
    fun stopReadAloudIfNot(articleID: String?) {
        val current = _currentReadAloudArticleId.value ?: return
        if (current != articleID) {
            dismiss()
        }
    }

    private fun ttsCacheDir(): File = File(context.cacheDir, "read_aloud")

    private suspend fun clearMedia() {
        mediaController?.let { controller ->
            if (controller.isConnected) {
                controller.stop()
                controller.clearMediaItems()
            }
        }
        _currentAudio.value = null
        _currentReadAloudArticleId.value = null
        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L
    }

    fun pause() {
        mainScope.launch {
            mediaController?.pause()
        }
    }

    fun resume() {
        mainScope.launch {
            mediaController?.play()
        }
    }

    /** Seek to a global position that spans every synthesized chunk. */
    fun seekTo(positionMs: Long) {
        mainScope.launch {
            mediaController?.let { controller ->
                PlaylistTimeline.seekToGlobal(controller, positionMs)
                _currentPosition.value = PlaylistTimeline.globalPosition(controller)
            }
        }
    }

    fun skipBack() {
        mainScope.launch {
            mediaController?.let { controller ->
                PlaylistTimeline.skip(controller, -SKIP_INTERVAL_MS)
                _currentPosition.value = PlaylistTimeline.globalPosition(controller)
            }
        }
    }

    fun skipForward() {
        mainScope.launch {
            mediaController?.let { controller ->
                PlaylistTimeline.skip(controller, SKIP_INTERVAL_MS)
                _currentPosition.value = PlaylistTimeline.globalPosition(controller)
            }
        }
    }

    fun dismiss() {
        synthJob?.cancel()
        mainScope.launch {
            mediaController?.let { controller ->
                controller.stop()
                controller.clearMediaItems()
            }
            _currentAudio.value = null
            _currentReadAloudArticleId.value = null
            _isPlaying.value = false
            _currentPosition.value = 0L
            _duration.value = 0L
            ttsCacheDir().deleteRecursively()
        }
    }

    fun release() {
        stopPositionUpdates()
        synthJob?.cancel()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
        mediaController = null
        controllerFuture = null
    }

    private fun startPositionUpdates() {
        stopPositionUpdates()
        positionUpdateJob = mainScope.launch {
            while (isActive) {
                mediaController?.let {
                    _currentPosition.value = PlaylistTimeline.globalPosition(it)
                    val known = PlaylistTimeline.knownDuration(it)
                    if (known > 0) {
                        _duration.value = known
                    }
                }
                delay(500)
            }
        }
    }

    private fun stopPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = null
    }

    companion object {
        private const val SKIP_INTERVAL_MS = 10_000L
    }
}
