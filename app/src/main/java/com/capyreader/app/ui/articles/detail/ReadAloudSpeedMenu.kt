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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.ui.components.ToolbarTooltip

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
    onSelectSpeed: (Float) -> Unit,
    onSelectPitch: (Float) -> Unit,
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
        }
    }
}

private fun formatSpeed(speed: Float): String {
    val text = if (speed % 1f == 0f) speed.toInt().toString() else speed.toString()
    return "${text}×"
}
