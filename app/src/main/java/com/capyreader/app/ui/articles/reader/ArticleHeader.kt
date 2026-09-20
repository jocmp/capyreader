package com.capyreader.app.ui.articles.reader

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.capyreader.app.ui.LocalTimeFormats
import com.capyreader.app.ui.articles.detail.byline
import com.capyreader.app.ui.articles.displayFeedName
import com.jocmp.capy.Article

@Composable
fun ArticleHeader(
    article: Article,
    onOpenLink: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val formats = LocalTimeFormats.current
    val readerStyle = LocalReaderStyle.current
    val feedName = article.displayFeedName(context)
    val showPlaceholderTitle = article.title.isBlank()
    val title = if (showPlaceholderTitle) {
        feedName
    } else {
        article.title
    }

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth(),
    ) {
        BidiLayoutDirection(paragraph = title) {
            Text(
                text = title,
                style = readerStyle.titleTextStyle.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenLink),
            )
        }
        Text(
            text = article.byline(context = context, formats = formats),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (!showPlaceholderTitle) {
            Text(
                text = feedName,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
