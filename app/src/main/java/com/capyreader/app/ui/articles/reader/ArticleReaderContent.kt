package com.capyreader.app.ui.articles.reader

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.AudioEnclosure
import com.jocmp.capy.Article
import com.jocmp.mallet.LinearArticle

private val MAX_READER_WIDTH = 640.dp

@Composable
fun ArticleReaderContent(
    article: Article,
    flattened: LinearArticle?,
    actions: ReaderActions,
    onOpenExternalLink: () -> Unit,
    currentAudioUrl: String?,
    isAudioPlaying: Boolean,
    onSelectAudio: (audio: AudioEnclosure) -> Unit,
    onPauseAudio: () -> Unit,
    modifier: Modifier = Modifier,
    onElementPositioned: (index: Int, coordinates: LayoutCoordinates) -> Unit = { _, _ -> },
) {
    val readerStyle = LocalReaderStyle.current
    val audioEnclosures = article.enclosures.filter { it.type.startsWith("audio/") }

    SelectionContainer {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .widthIn(max = MAX_READER_WIDTH)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp),
        ) {
            ArticleHeader(
                article = article,
                onOpenLink = onOpenExternalLink,
            )

            audioEnclosures.forEach { enclosure ->
                AudioEnclosureCard(
                    article = article,
                    enclosure = enclosure,
                    isPlaying = isAudioPlaying && currentAudioUrl == enclosure.url.toString(),
                    onPlay = onSelectAudio,
                    onPause = onPauseAudio,
                )
            }

            if (flattened != null) {
                ProvideTextStyle(
                    readerStyle.bodyTextStyle.copy(color = MaterialTheme.colorScheme.onSurface)
                ) {
                    ArticleBody(
                        article = flattened,
                        actions = actions,
                        onElementPositioned = onElementPositioned,
                    )
                }

                if (flattened.truncated != null) {
                    TruncationNotice(onOpenLink = onOpenExternalLink)
                }
            }

            Spacer(Modifier.height(80.dp))
        }
    }
    }
}

@Composable
private fun TruncationNotice(onOpenLink: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = stringResource(R.string.reader_article_truncated),
                style = MaterialTheme.typography.bodyMedium,
            )
            TextButton(onClick = onOpenLink) {
                Text(text = stringResource(R.string.reader_open_in_browser))
            }
        }
    }
}
