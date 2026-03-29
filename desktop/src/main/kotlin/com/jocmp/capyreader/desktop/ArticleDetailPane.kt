package com.jocmp.capyreader.desktop

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.jocmp.capy.Article
import java.awt.Desktop
import java.net.URI
import java.time.format.DateTimeFormatter
import javax.swing.JEditorPane
import javax.swing.JScrollPane
import javax.swing.event.HyperlinkEvent

private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy 'at' h:mm a")

@Composable
fun ArticleDetailPane(
    state: ReaderState,
    modifier: Modifier = Modifier,
) {
    val article by state.selectedArticle.collectAsDesktopState()

    Surface(modifier = modifier.fillMaxSize()) {
        val current = article
        if (current == null) {
            EmptyDetail()
        } else {
            ArticleDetail(
                article = current,
                onToggleRead = { state.toggleRead() },
                onToggleStar = { state.toggleStar() },
            )
        }
    }
}

@Composable
private fun EmptyDetail() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = "Select an article to read",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ArticleDetail(
    article: Article,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
) {
    val isDark = isSystemInDarkTheme()
    val bgColor = MaterialTheme.colorScheme.surface
    val textColor = MaterialTheme.colorScheme.onSurface
    val mutedColor = MaterialTheme.colorScheme.onSurfaceVariant
    val linkColor = MaterialTheme.colorScheme.primary

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(start = 8.dp),
            ) {
                Text(
                    text = article.feedName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                article.author?.let { author ->
                    Text(
                        text = " \u00B7 $author",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }

            Row {
                IconButton(onClick = onToggleRead) {
                    Icon(
                        imageVector = if (article.read) Icons.Outlined.Circle else Icons.Rounded.Circle,
                        contentDescription = if (article.read) "Mark unread" else "Mark read",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                IconButton(onClick = onToggleStar) {
                    Icon(
                        imageVector = if (article.starred) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                        contentDescription = if (article.starred) "Unstar" else "Star",
                        tint = if (article.starred) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                article.url?.let { url ->
                    IconButton(onClick = { openInBrowser(url.toString()) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                            contentDescription = "Open in browser",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }

        HorizontalDivider()

        key(article.id) {
            val htmlContent = buildArticleHtml(
                article = article,
                bgHex = bgColor.toHex(),
                textHex = textColor.toHex(),
                mutedHex = mutedColor.toHex(),
                linkHex = linkColor.toHex(),
            )

            SwingPanel(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    val editorPane = JEditorPane().apply {
                        contentType = "text/html"
                        isEditable = false
                        putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, true)

                        addHyperlinkListener { event ->
                            if (event.eventType == HyperlinkEvent.EventType.ACTIVATED) {
                                openInBrowser(event.url.toString())
                            }
                        }

                        text = htmlContent
                        caretPosition = 0
                    }

                    val bgAwt = java.awt.Color(bgColor.toArgb())
                    editorPane.background = bgAwt

                    JScrollPane(editorPane).apply {
                        border = null
                        background = bgAwt
                        viewport.background = bgAwt
                    }
                },
            )
        }
    }
}

private fun buildArticleHtml(
    article: Article,
    bgHex: String,
    textHex: String,
    mutedHex: String,
    linkHex: String,
): String {
    val date = article.publishedAt.format(dateFormatter)

    return """
        <html>
        <head>
        <style>
            body {
                font-family: -apple-system, "Segoe UI", "Helvetica Neue", Helvetica, Arial, sans-serif;
                font-size: 15px;
                line-height: 1.6;
                color: $textHex;
                background: $bgHex;
                margin: 0;
                padding: 24px 32px;
                max-width: 720px;
            }
            h1 { font-size: 24px; line-height: 1.3; margin: 0 0 8px 0; }
            .meta { color: $mutedHex; font-size: 13px; margin-bottom: 16px; }
            hr { border: none; border-top: 1px solid ${mutedHex}44; margin: 16px 0; }
            a { color: $linkHex; }
            img { max-width: 100%; height: auto; border-radius: 4px; margin: 8px 0; }
            pre, code {
                background: ${mutedHex}18;
                border-radius: 4px;
                padding: 2px 6px;
                font-size: 13px;
            }
            pre { padding: 12px; overflow-x: auto; }
            pre code { background: none; padding: 0; }
            blockquote {
                border-left: 3px solid $linkHex;
                margin: 12px 0;
                padding: 4px 16px;
                color: $mutedHex;
            }
            table { border-collapse: collapse; margin: 12px 0; }
            td, th { border: 1px solid ${mutedHex}44; padding: 6px 10px; }
            figure { margin: 12px 0; }
            figcaption { color: $mutedHex; font-size: 13px; text-align: center; }
        </style>
        </head>
        <body>
            <h1>${escapeHtmlEntities(article.title)}</h1>
            <div class="meta">${date}</div>
            <hr>
            ${article.content}
        </body>
        </html>
    """.trimIndent()
}

private fun escapeHtmlEntities(text: String): String {
    return text
        .replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
}

private fun androidx.compose.ui.graphics.Color.toHex(): String {
    val argb = toArgb()
    return String.format("#%06X", argb and 0xFFFFFF)
}

private fun openInBrowser(url: String) {
    try {
        if (Desktop.isDesktopSupported()) {
            Desktop.getDesktop().browse(URI(url))
        }
    } catch (_: Exception) {
        // Silently fail
    }
}
