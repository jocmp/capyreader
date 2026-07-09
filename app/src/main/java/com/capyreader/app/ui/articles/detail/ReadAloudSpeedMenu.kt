package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import android.speech.tts.TextToSpeech
import android.speech.tts.Voice
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.components.ToolbarTooltip
import kotlinx.coroutines.CompletableDeferred

private val SPEED_OPTIONS = listOf(0.75f, 1.0f, 1.25f, 1.5f, 1.75f, 2.0f)

private val PITCH_OPTIONS = listOf(
    R.string.read_aloud_pitch_low to 0.8f,
    R.string.read_aloud_pitch_normal to 1.0f,
    R.string.read_aloud_pitch_high to 1.2f,
)

/**
 * Toolbar control for read-aloud playback speed (and pitch). Opens a dropdown of
 * speed multipliers plus a pitch selector. Selections are persisted by the caller.
 */
@Composable
fun ReadAloudSpeedMenu(
    speed: Float,
    pitch: Float,
    voice: String,
    onSelectSpeed: (Float) -> Unit,
    onSelectPitch: (Float) -> Unit,
    onSelectVoice: (String) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }
    val label = stringResource(R.string.read_aloud_speed)

    Box {
        ToolbarTooltip(message = label) {
            IconButton(onClick = { expanded = true }) {
                Icon(
                    Icons.Outlined.Speed,
                    contentDescription = label,
                    modifier = Modifier.size(24.dp),
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            SPEED_OPTIONS.forEach { option ->
                DropdownMenuItem(
                    text = { Text(formatSpeed(option)) },
                    trailingIcon = {
                        if (option == speed) {
                            Icon(Icons.Outlined.Check, contentDescription = null)
                        }
                    },
                    onClick = {
                        onSelectSpeed(option)
                        expanded = false
                    },
                )
            }

            HorizontalDivider()

            Text(
                text = stringResource(R.string.read_aloud_pitch),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            PITCH_OPTIONS.forEach { (labelRes, option) ->
                DropdownMenuItem(
                    text = { Text(stringResource(labelRes)) },
                    trailingIcon = {
                        if (option == pitch) {
                            Icon(Icons.Outlined.Check, contentDescription = null)
                        }
                    },
                    onClick = {
                        onSelectPitch(option)
                        expanded = false
                    },
                )
            }

            HorizontalDivider()

            Text(
                text = stringResource(R.string.read_aloud_voice),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.read_aloud_voice_automatic)) },
                trailingIcon = {
                    if (voice.isBlank()) {
                        Icon(Icons.Outlined.Check, contentDescription = null)
                    }
                },
                onClick = {
                    onSelectVoice("")
                    expanded = false
                },
            )
            // Enumerated only while the menu is open (see rememberReadAloudVoices).
            rememberReadAloudVoices().forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.label) },
                    trailingIcon = {
                        if (option.name == voice) {
                            Icon(Icons.Outlined.Check, contentDescription = null)
                        }
                    },
                    onClick = {
                        onSelectVoice(option.name)
                        expanded = false
                    },
                )
            }
        }
    }
}

private data class VoiceOption(val name: String, val label: String, val network: Boolean)

/**
 * Installed English TTS voices for the picker. Spins up a short-lived
 * [TextToSpeech] to enumerate voices and shuts it down when the menu closes, so
 * there's no always-on engine. Neural (network) voices are listed first;
 * `notInstalled` voices are skipped. Returns empty until the engine has bound.
 */
@Composable
private fun rememberReadAloudVoices(): List<VoiceOption> {
    val context = LocalContext.current
    val voices by produceState(initialValue = emptyList<VoiceOption>()) {
        var engine: TextToSpeech? = null
        val result = CompletableDeferred<List<VoiceOption>>()
        engine = TextToSpeech(context.applicationContext) { status ->
            val tts = engine
            val list = if (status == TextToSpeech.SUCCESS && tts != null) {
                runCatching { tts.voices }.getOrNull().orEmpty()
                    .filter {
                        it.locale.language.equals("en", true) &&
                            it.features?.contains(TextToSpeech.Engine.KEY_FEATURE_NOT_INSTALLED) != true
                    }
                    .map { VoiceOption(it.name, voiceLabel(it), it.isNetworkConnectionRequired) }
                    .distinctBy { it.name }
                    .sortedWith(compareByDescending<VoiceOption> { it.network }.thenBy { it.label })
            } else {
                emptyList()
            }
            result.complete(list)
        }
        value = result.await()
        awaitDispose { engine?.shutdown() }
    }
    return voices
}

private fun voiceLabel(voice: Voice): String {
    val country = when (voice.locale.country.uppercase()) {
        "US" -> "US"
        "GB" -> "UK"
        "AU" -> "Australia"
        "IN" -> "India"
        "NG" -> "Nigeria"
        else -> voice.locale.country.ifBlank { "English" }
    }
    val kind = if (voice.isNetworkConnectionRequired) "Neural" else "On-device"
    val variant = voice.name.substringAfter("-x-", "").substringBefore("-")
    return if (variant.isBlank()) "$country · $kind" else "$country · $kind ($variant)"
}

private fun formatSpeed(speed: Float): String {
    val text = if (speed % 1f == 0f) speed.toInt().toString() else speed.toString()
    return "${text}×"
}
