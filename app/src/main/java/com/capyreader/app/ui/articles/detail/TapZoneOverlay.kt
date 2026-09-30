package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import com.capyreader.app.R

@Composable
fun TapZoneOverlay(
    edgeFraction: Float,
    onDismiss: () -> Unit,
) {
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val colors = MaterialTheme.colorScheme

    Row(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    val down = awaitFirstDown(pass = PointerEventPass.Initial)
                    down.consume()
                    currentOnDismiss()
                }
            }
    ) {
        TapZone(
            label = stringResource(R.string.reader_tap_zone_previous),
            color = colors.inverseSurface.copy(alpha = EDGE_ALPHA),
            textColor = colors.inverseOnSurface,
            modifier = Modifier.weight(edgeFraction),
        )
        TapZone(
            label = stringResource(R.string.reader_tap_zone_menu),
            color = colors.surfaceVariant.copy(alpha = MENU_ALPHA),
            textColor = colors.onSurfaceVariant,
            modifier = Modifier.weight(1f - edgeFraction * 2),
        )
        TapZone(
            label = stringResource(R.string.reader_tap_zone_next),
            color = colors.inverseSurface.copy(alpha = EDGE_ALPHA),
            textColor = colors.inverseOnSurface,
            modifier = Modifier.weight(edgeFraction),
        )
    }
}

@Composable
private fun TapZone(
    label: String,
    color: Color,
    textColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxHeight()
            .background(color),
    ) {
        Text(
            text = label,
            color = textColor,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
        )
    }
}

private const val EDGE_ALPHA = 0.8f

private const val MENU_ALPHA = 0.8f
