package com.jocmp.capyreader.ui.articles.detail

import androidx.compose.foundation.layout.Row
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import com.jocmp.capy.Article
import com.jocmp.capyreader.R
import com.jocmp.capyreader.common.shareArticle
import com.jocmp.capyreader.ui.fixtures.ArticleSample
import com.jocmp.capyreader.ui.isCompact
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleTopBar(
    article: Article?,
    extractedContent: ExtractedContent,
    onToggleExtractContent: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current

    TopAppBar(
        navigationIcon = {
            ArticleNavigationIcon(
                onClick = onClose
            )
        },
        title = {},
        actions = {
            Row {
                if (article != null) {
                    IconButton(onClick = { onToggleRead() }) {
                        Icon(
                            painterResource(id = readIcon(article)),
                            contentDescription = stringResource(R.string.article_view_mark_as_read)
                        )
                    }

                    if (article.extractedContentURL != null) {
                        IconButton(onClick = { onToggleExtractContent() }) {
                            Icon(
                                painterResource(id = extractIcon(extractedContent)),
                                contentDescription = stringResource(R.string.extract_full_content)
                            )
                        }
                    }

                    IconButton(onClick = { onToggleStar() }) {
                        Icon(
                            painterResource(id = starredIcon(article)),
                            contentDescription = stringResource(R.string.article_view_star)
                        )
                    }

                    IconButton(onClick = { context.shareArticle(article = article) }) {
                        Icon(
                            painterResource(id = R.drawable.ic_share),
                            contentDescription = stringResource(R.string.article_share)
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun readIcon(article: Article) =
    if (article.read) {
        R.drawable.icon_circle_outline
    } else {
        R.drawable.icon_circle_filled
    }

@Composable
fun starredIcon(article: Article) =
    if (article.starred) {
        R.drawable.icon_star_filled
    } else {
        R.drawable.icon_star_outline
    }

@Composable
fun extractIcon(extractedContent: ExtractedContent) = when {
    extractedContent.isComplete -> R.drawable.icon_article_filled
    else -> R.drawable.icon_article_empty
}


@Composable
fun ArticleNavigationIcon(onClick: () -> Unit) {
    val showIcon = isCompact()

    if (!showIcon) {
        return
    }

    IconButton(
        onClick = {
            onClick()
        }
    ) {
        Icon(
            imageVector = Icons.Filled.Close,
            contentDescription = null
        )
    }
}

@Preview
@Composable
private fun ArticleTopBarPreview(@PreviewParameter(ArticleSample::class) article: Article) {
    ArticleTopBar(
        article = article.copy(extractedContentURL = URL("https://example.com")),
        extractedContent = ExtractedContent(),
        onToggleExtractContent = {},
        onToggleRead = {},
        onToggleStar = {},
        onClose = {},
    )
}

@Preview(device = "id:pixel_fold")
@Composable
private fun ArticleTopBarPreview_Tablet(@PreviewParameter(ArticleSample::class) article: Article) {
    ArticleTopBar(
        article = article,
        extractedContent = ExtractedContent(),
        onToggleExtractContent = {},
        onToggleRead = {},
        onToggleStar = {},
        onClose = {}
    )
}

@Preview
@Composable
private fun ArticleTopBarPreview_MissingArticle() {
    ArticleTopBar(
        article = null,
        extractedContent = ExtractedContent(),
        onToggleExtractContent = {},
        onToggleRead = {},
        onToggleStar = {},
        onClose = {}
    )
}
