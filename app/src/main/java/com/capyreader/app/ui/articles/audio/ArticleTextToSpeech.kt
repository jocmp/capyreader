package com.capyreader.app.ui.articles.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.core.text.HtmlCompat
import com.jocmp.capy.logging.CapyLog
import kotlinx.coroutines.CompletableDeferred
import java.io.File
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap

/**
 * Renders article text to audio files using the platform [TextToSpeech] engine.
 *
 * The article body is HTML, so it is converted to plain text and split into
 * utterance-sized chunks (a single TTS utterance is capped at
 * [TextToSpeech.getMaxSpeechInputLength], typically 4000 chars). Each chunk is
 * synthesized to a WAV file via [TextToSpeech.synthesizeToFile]. Those files are
 * then played back through the shared media3 pipeline ([MediaPlaybackService]),
 * which is what gives read-aloud a MediaSession, notification/lock-screen
 * controls, and screen-off/Doze survival.
 *
 * Speech rate and pitch are applied on the engine before synthesis, per
 * [TextToSpeech.setSpeechRate] / [TextToSpeech.setPitch].
 */
class ArticleTextToSpeech(context: Context) {
    private val ready = CompletableDeferred<Boolean>()
    private val pending = ConcurrentHashMap<String, CompletableDeferred<Boolean>>()

    private val tts = TextToSpeech(context.applicationContext) { status ->
        ready.complete(status == TextToSpeech.SUCCESS)
        if (status != TextToSpeech.SUCCESS) {
            CapyLog.warn("read_aloud_init", mapOf("status" to status.toString()))
        }
    }

    init {
        tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {}

            override fun onDone(utteranceId: String?) {
                pending.remove(utteranceId)?.complete(true)
            }

            @Deprecated("Deprecated in Java")
            override fun onError(utteranceId: String?) {
                pending.remove(utteranceId)?.complete(false)
            }

            override fun onError(utteranceId: String?, errorCode: Int) {
                pending.remove(utteranceId)?.complete(false)
            }
        })
    }

    /**
     * Prepare the engine for a read: wait for init, then apply language, rate and
     * pitch (which affect every subsequent utterance). Returns false if the engine
     * failed to initialize. Call from a background dispatcher.
     */
    suspend fun begin(speed: Float, pitch: Float): Boolean {
        if (!ready.await()) return false
        tts.language = Locale.getDefault()
        tts.setSpeechRate(speed)
        tts.setPitch(pitch)
        return true
    }

    /**
     * Synthesize a single chunk to [outputDir] and suspend until it is rendered.
     * Returns the WAV file on success, or null on failure. This is what enables
     * progressive playback: chunk 0 can play while later chunks render.
     */
    suspend fun synthesize(chunk: String, index: Int, outputDir: File): File? {
        val utteranceId = "read_aloud_$index"
        val file = File(outputDir, "read_aloud_$index.wav")
        val deferred = CompletableDeferred<Boolean>()
        pending[utteranceId] = deferred

        val result = tts.synthesizeToFile(chunk, Bundle(), file, utteranceId)
        if (result != TextToSpeech.SUCCESS) {
            pending.remove(utteranceId)
            return null
        }

        return if (deferred.await() && file.exists() && file.length() > 0) file else null
    }

    fun shutdown() {
        pending.values.forEach { it.complete(false) }
        pending.clear()
        tts.stop()
        tts.shutdown()
    }

    companion object {
        // Stay under TextToSpeech.getMaxSpeechInputLength() (~4000).
        private const val MAX_CHUNK = 3500

        fun plainText(html: String): String =
            HtmlCompat.fromHtml(html, HtmlCompat.FROM_HTML_MODE_COMPACT)
                .toString()
                .replace(Regex("\\n{2,}"), "\n")
                .trim()

        /** Split into <= [MAX_CHUNK] pieces, breaking on sentence/whitespace boundaries. */
        fun chunk(text: String): List<String> {
            if (text.length <= MAX_CHUNK) {
                return if (text.isBlank()) emptyList() else listOf(text)
            }

            val chunks = mutableListOf<String>()
            var start = 0
            while (start < text.length) {
                var end = minOf(start + MAX_CHUNK, text.length)
                if (end < text.length) {
                    val boundary =
                        text.lastIndexOfAny(charArrayOf('.', '!', '?', '\n', ' '), end - 1)
                    if (boundary > start) end = boundary + 1
                }
                chunks.add(text.substring(start, end).trim())
                start = end
            }
            return chunks.filter { it.isNotBlank() }
        }
    }
}
