package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.shareArticle
import com.capyreader.app.ui.articles.FullContentLoadingIcon
import com.capyreader.app.ui.fixtures.ArticleSample
import com.capyreader.app.ui.fixtures.InjectedCapyTheme
import com.capyreader.app.ui.isAtMostMedium
import com.jocmp.capy.Article
import com.jocmp.capy.articles.ExtractedContent
import java.net.URL

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleTopBar(
    article: Article?,
    extractedContent: ExtractedContent,
    onToggleExtractContent: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val (isStyleSheetOpen, setStyleSheetOpen) = rememberSaveable { mutableStateOf(false) }

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

                    IconButton(onClick = { onToggleExtractContent() }) {
                        if (extractedContent.isLoading) {
                            FullContentLoadingIcon()
                        } else {
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

                    IconButton(onClick = { setStyleSheetOpen(true) }) {
                        Icon(
                            Icons.Outlined.FormatSize,
                            stringResource(R.string.article_style_options)
                        )
                    }

                    IconButton(onClick = { context.shareArticle(article = article) }) {
                        Icon(
                            Icons.Rounded.Share,
                            contentDescription = stringResource(R.string.article_share)
                        )
                    }
                }
            }
        }
    )


    if (isStyleSheetOpen) {
        ModalBottomSheet(onDismissRequest = { setStyleSheetOpen(false) }) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 32.dp)
                    .padding(bottom = 16.dp)
            ) {
                ArticleStylePicker()
            }
        }
    }
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
    val showIcon = isAtMostMedium()

    if (!showIcon) {
        return
    }

    IconButton(
        onClick = {
            onClick()
        }
    ) {
        Icon(
            imageVector = Icons.Rounded.Close,
            contentDescription = null
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ArticleTopBarPreview(@PreviewParameter(ArticleSample::class) article: Article) {
    InjectedCapyTheme {
        ArticleTopBar(
            article = article.copy(extractedContentURL = URL("https://example.com")),
            extractedContent = ExtractedContent(),
            onToggleExtractContent = {},
            onToggleRead = {},
            onToggleStar = {},
            onClose = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(device = "id:pixel_fold")
@Composable
private fun ArticleTopBarPreview_Tablet(@PreviewParameter(ArticleSample::class) article: Article) {
    InjectedCapyTheme {
        ArticleTopBar(
            article = article,
            extractedContent = ExtractedContent(),
            onToggleExtractContent = {},
            onToggleRead = {},
            onToggleStar = {},
            onClose = {}
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ArticleTopBarPreview_MissingArticle() {
    InjectedCapyTheme {
        ArticleTopBar(
            article = null,
            extractedContent = ExtractedContent(),
            onToggleExtractContent = {},
            onToggleRead = {},
            onToggleStar = {},
            onClose = {}
        )
    }
}
