package com.capyreader.app.ui.articles.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.capyreader.app.R
import com.capyreader.app.common.AudioEnclosure
import com.jocmp.capy.Article
import com.jocmp.capy.Enclosure

@Composable
fun AudioEnclosureCard(
    article: Article,
    enclosure: Enclosure,
    isPlaying: Boolean,
    isBuffering: Boolean,
    onPlay: (audio: AudioEnclosure) -> Unit,
    onPause: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val url = enclosure.url.toString()
    val artworkUrl = enclosure.itunesImage?.ifBlank { null }
    val duration = enclosure.itunesDurationSeconds?.let { formatDuration(it) }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(12.dp),
        ) {
            Artwork(artworkUrl = artworkUrl)
            Column(
                verticalArrangement = Arrangement.spacedBy(2.dp),
                modifier = Modifier.weight(1f),
            ) {
                Text(
                    text = article.title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = article.feedName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                if (duration != null) {
                    Text(
                        text = duration,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            PlayPauseButton(
                isPlaying = isPlaying,
                isBuffering = isBuffering,
                onClick = {
                    if (isPlaying || isBuffering) {
                        onPause()
                    } else {
                        onPlay(
                            AudioEnclosure(
                                articleID = article.id,
                                url = url,
                                title = article.title,
                                feedName = article.feedName,
                                durationSeconds = enclosure.itunesDurationSeconds,
                                artworkUrl = artworkUrl,
                            )
                        )
                    }
                },
            )
        }
    }
}

@Composable
private fun Artwork(artworkUrl: String?) {
    val shape = MaterialTheme.shapes.small

    if (artworkUrl == null) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(64.dp)
                .clip(shape),
        ) {
            Icon(
                imageVector = Icons.Outlined.Audiotrack,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(32.dp),
            )
        }
        return
    }

    AsyncImage(
        model = artworkUrl,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .size(64.dp)
            .clip(shape),
    )
}

@Composable
private fun PlayPauseButton(isPlaying: Boolean, isBuffering: Boolean, onClick: () -> Unit) {
    val showPause = isPlaying || isBuffering
    val icon = if (showPause) {
        Icons.Filled.Pause
    } else {
        Icons.Filled.PlayArrow
    }
    val label = if (showPause) {
        stringResource(R.string.audio_player_pause)
    } else {
        stringResource(R.string.audio_player_play)
    }

    Box(contentAlignment = Alignment.Center) {
        FilledIconButton(onClick = onClick) {
            Icon(imageVector = icon, contentDescription = label)
        }
        if (isBuffering) {
            CircularProgressIndicator(
                strokeWidth = 3.dp,
                modifier = Modifier.size(48.dp),
            )
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    val secs = seconds % 60

    if (hours > 0) {
        return "%d:%02d:%02d".format(hours, minutes, secs)
    }

    return "%d:%02d".format(minutes, secs)
}
