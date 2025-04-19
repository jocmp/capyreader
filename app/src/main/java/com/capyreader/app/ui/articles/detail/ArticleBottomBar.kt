@file:OptIn(ExperimentalMaterial3Api::class)

package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.BottomAppBarDefaults.exitAlwaysScrollBehavior
import androidx.compose.material3.BottomAppBarScrollBehavior
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
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
import com.capyreader.app.ui.components.ToolbarTooltip
import com.capyreader.app.ui.fixtures.ArticleSample
import com.capyreader.app.ui.fixtures.PreviewKoinApplication
import com.jocmp.capy.Article
import com.jocmp.capy.Article.FullContentState.ERROR
import com.jocmp.capy.Article.FullContentState.LOADED
import com.jocmp.capy.Article.FullContentState.LOADING
import java.net.URL

@Composable
fun ArticleBottomBar(
    article: Article?,
    scrollBehavior: BottomAppBarScrollBehavior = exitAlwaysScrollBehavior(),
    onToggleExtractContent: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
) {
    val context = LocalContext.current
    val (isStyleSheetOpen, setStyleSheetOpen) = rememberSaveable { mutableStateOf(false) }

    BottomAppBar(
        scrollBehavior = scrollBehavior,
        actions = {
            if (article != null) {
                ToolbarTooltip(
                    message = stringResource(R.string.article_view_mark_as_read)
                ) {
                    IconButton(
                        onClick = { onToggleRead() },
                    ) {
                        Icon(
                            readIcon(article),
                            contentDescription = stringResource(R.string.article_view_mark_as_read)
                        )
                    }
                }
                ToolbarTooltip(
                    message = stringResource(R.string.article_view_star)
                ) {
                    IconButton(
                        onClick = { onToggleStar() },

                        ) {
                        Icon(
                            starredIcon(article),
                            contentDescription = stringResource(R.string.article_view_star)
                        )
                    }
                }
                ToolbarTooltip(
                    message = stringResource(R.string.extract_full_content)
                ) {
                    IconButton(
                        onClick = { onToggleExtractContent() },
                    ) {
                        if (article.fullContent == LOADING) {
                            FullContentLoadingIcon()
                        } else {
                            Icon(
                                painterResource(id = extractIcon(article.fullContent)),
                                contentDescription = stringResource(R.string.extract_full_content)
                            )
                        }
                    }
                }
                ToolbarTooltip(
                    message = stringResource(R.string.article_style_options)
                ) {
                    IconButton(
                        onClick = { setStyleSheetOpen(true) },
                    ) {
                        Icon(
                            Icons.Outlined.FormatSize,
                            stringResource(R.string.article_style_options)
                        )
                    }
                }
                ToolbarTooltip(
                    message = stringResource(R.string.article_share)
                ) {
                    IconButton(
                        onClick = { context.shareArticle(article = article) },
                    ) {
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
        Icons.Outlined.Circle
    } else {
        Icons.Rounded.Circle
    }

@Composable
fun starredIcon(article: Article) =
    if (article.starred) {
        Icons.Rounded.Star
    } else {
        Icons.Rounded.StarOutline
    }

@Composable
fun extractIcon(fullContentState: Article.FullContentState) = when (fullContentState) {
    LOADED -> R.drawable.icon_article_filled
    Article.FullContentState.ERROR -> R.drawable.icon_article_error
    else -> R.drawable.icon_article_empty
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ArticleBottomBarPreview(@PreviewParameter(ArticleSample::class) article: Article) {
    PreviewKoinApplication {
        ArticleBottomBar(
            article = article.copy(
                extractedContentURL = URL("https://example.com"),
            ),
            onToggleExtractContent = {},
            onToggleRead = {},
            onToggleStar = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ArticleBottomBarPreviewError(@PreviewParameter(ArticleSample::class) article: Article) {
    PreviewKoinApplication {
        ArticleBottomBar(
            article = article.copy(
                extractedContentURL = URL("https://example.com"),
                fullContent = ERROR
            ),
            onToggleExtractContent = {},
            onToggleRead = {},
            onToggleStar = {},
        )
    }
}
