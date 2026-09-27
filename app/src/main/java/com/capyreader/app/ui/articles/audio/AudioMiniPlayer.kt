package com.capyreader.app.ui.articles.audio

import androidx.annotation.OptIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.compose.material3.MiniController
import androidx.media3.ui.compose.material3.buttons.PlayPauseButton
import androidx.media3.ui.compose.material3.buttons.SeekBackButton
import com.capyreader.app.R

@OptIn(UnstableApi::class)
@Composable
fun AudioMiniPlayer(
    player: Player,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val bitmapLoader = remember(context) {
        CoilBitmapLoader(context.applicationContext)
    }
    val headphones = rememberVectorPainter(image = Icons.Rounded.Headphones)
    val artworkTint = MaterialTheme.colorScheme.onSurfaceVariant
    val defaultArtwork = remember(headphones, artworkTint) {
        TintedPainter(painter = headphones, tint = artworkTint)
    }

    MiniController(
        player = player,
        bitmapLoader = bitmapLoader,
        defaultArtwork = defaultArtwork,
        onClick = onClick,
        modifier = modifier.padding(horizontal = 8.dp),
        playerControls = { player ->
            SeekBackButton(player = player)
            PlayPauseButton(player = player)
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.audio_player_close),
                )
            }
        },
    )
}

private class TintedPainter(
    private val painter: Painter,
    private val tint: Color,
) : Painter() {
    override val intrinsicSize: Size
        get() = painter.intrinsicSize

    override fun DrawScope.onDraw() {
        with(painter) {
            draw(size = size, colorFilter = ColorFilter.tint(tint))
        }
    }
}
