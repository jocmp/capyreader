package com.capyreader.app.ui.articles.audio

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.CenterVertically
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.capyreader.app.ui.theme.CapyTheme
import com.jocmp.capy.Enclosure
import com.jocmp.capy.articles.Podcast
import java.net.URL

@Composable
fun AudioEmbedView(podcast: Podcast) {
    var isPlaying by remember { mutableStateOf(false) }

    Surface(
        color = colorScheme.surfaceContainer,
        modifier = Modifier
            .height(84.dp)
            .padding(vertical = 8.dp, horizontal = 16.dp)
            .clip(RoundedCornerShape(8.dp))
    ) {
        Row(
            verticalAlignment = CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            podcast.enclosure.itunesImage?.let {
                AsyncImage(
                    model = it,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier =
                        Modifier
                            .aspectRatio(1f)
                            .background(colorScheme.surfaceContainer)
                )
            }

            Column(
                Modifier.weight(0.1f),
            ) {
                Text(
                    podcast.feedName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                )
                Text(
                    podcast.title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontSize = 14.sp,
                )
            }

            IconButton(
                onClick = { isPlaying = !isPlaying },
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play"
                )
            }
        }
    }
}

@Preview
@Composable
fun AudioEmbedViewPreview() {
    CapyTheme {
        Box(
            Modifier.padding(16.dp)
        ) {
            AudioEmbedView(
                podcast = Podcast(
                    articleID = "123",
                    title = "The movie and TV tech we actually want to use",
                    feedName = "The Vergecast",
                    enclosure = Enclosure(
                        url = URL("https://example.com"),
                        type = "audio/mpeg",
                        itunesDurationSeconds = 3601,
                        itunesImage = null,
                    )
                )
            )
        }
    }
}