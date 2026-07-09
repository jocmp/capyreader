package com.capyreader.app.ui.articles.audio

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.speech.tts.Voice
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

    // Offline auto-fallback: if the chosen neural voice needs the network and a
    // chunk fails to synthesize (e.g. reading offline), switch once to an
    // on-device voice and retry, so read-aloud still works without a connection.
    private var fallbackVoice: Voice? = null
    private var chosenIsNetwork = false
    private var fellBackToLocal = false

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
    suspend fun begin(speed: Float, pitch: Float, voiceName: String = ""): Boolean {
        if (!ready.await()) return false
        fellBackToLocal = false
        tts.language = Locale.getDefault()
        applyBestVoice(Locale.getDefault(), voiceName)
        tts.setSpeechRate(speed)
        tts.setPitch(pitch)
        return true
    }

    /**
     * Upgrade from the engine's default voice (usually a low-quality on-device
     * pack) to a good neural voice. On Google TTS every English voice reports the
     * same quality (HIGH), so [Voice.getQuality] can't rank them — the real split
     * is the streamed `-network` (neural) voices vs the robotic `-local` ones,
     * and the neural ones aren't selectable in Android's system TTS settings at
     * all, only through this API.
     *
     * We pick deterministically: a curated neural voice from [PREFERRED_VOICES]
     * if installed, else any installed network voice, else any installed voice
     * for the reading language. `notInstalled` voices are skipped (their data
     * isn't downloaded). The chosen neural voice needs a network connection at
     * synthesis time — fine here since chunks are synthesized up front, so
     * connectivity is only needed while generating, not while listening.
     */
    private fun applyBestVoice(preferred: Locale, preferredName: String) {
        val voices = try {
            tts.voices
        } catch (e: Exception) {
            CapyLog.warn("read_aloud_voices", mapOf("error" to e.message))
            null
        } ?: return

        val lang = if (preferred.language.equals("en", true)) "en" else preferred.language
        val installed = voices.filter {
            it.locale.language.equals(lang, true) &&
                it.features?.contains(NOT_INSTALLED) != true
        }
        if (installed.isEmpty()) return

        val byName = installed.associateBy { it.name }
        // The user's explicit pick wins (if still installed); otherwise auto-pick.
        val chosen = byName[preferredName]
            ?: PREFERRED_VOICES.firstNotNullOfOrNull { byName[it] }
            ?: installed.filter { it.isNetworkConnectionRequired }.minByOrNull { it.name }
            ?: installed.minByOrNull { it.name }
            ?: return

        // Offline fallback = the on-device twin of the chosen voice if present
        // (its "-network" name with "-local"), else any installed local voice.
        chosenIsNetwork = chosen.isNetworkConnectionRequired
        fallbackVoice = if (chosenIsNetwork) {
            byName[chosen.name.replace("-network", "-local")]
                ?: installed.filterNot { it.isNetworkConnectionRequired }.minByOrNull { it.name }
        } else {
            null
        }

        val result = tts.setVoice(chosen)
        CapyLog.info(
            "read_aloud_voice",
            mapOf(
                "voice" to chosen.name,
                "network" to chosen.isNetworkConnectionRequired.toString(),
                "set" to result.toString(),
            )
        )
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

        val ok = deferred.await() && file.exists() && file.length() > 0
        if (ok) return file

        // The network voice failed (likely offline). Fall back to the on-device
        // voice once and retry this chunk; subsequent chunks reuse the local
        // voice too, so a whole article still reads without connectivity.
        if (chosenIsNetwork && !fellBackToLocal) {
            val local = fallbackVoice
            if (local != null) {
                fellBackToLocal = true
                tts.setVoice(local)
                CapyLog.info("read_aloud_fallback", mapOf("voice" to local.name))
                return synthesize(chunk, index, outputDir)
            }
        }
        return null
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

        private val NOT_INSTALLED = TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED

        // Curated Google US-English neural (network) voices, best-first. All
        // report the same quality metadata, so this order is by listening
        // preference; the first one installed on the device wins. Falls back to
        // any installed network voice if none of these are present.
        private val PREFERRED_VOICES = listOf(
            "en-us-x-iol-network",
            "en-us-x-tpf-network",
            "en-us-x-iog-network",
            "en-us-x-iom-network",
            "en-us-x-tpd-network",
            "en-us-x-tpc-network",
            "en-us-x-iob-network",
            "en-us-x-sfg-network",
        )

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
