@file:OptIn(ExperimentalMaterial3Api::class)

package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior
import androidx.compose.material3.TopAppBarScrollBehavior
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
import com.capyreader.app.ui.components.TopBarTooltip
import com.capyreader.app.ui.fixtures.ArticleSample
import com.capyreader.app.ui.fixtures.InjectedCapyTheme
import com.jocmp.capy.Article
import com.jocmp.capy.Article.FullContentState.LOADED
import com.jocmp.capy.Article.FullContentState.LOADING
import java.net.URL

@Composable
fun ArticleTopBar(
    article: Article?,
    scrollBehavior: TopAppBarScrollBehavior,
    onToggleExtractContent: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val (isStyleSheetOpen, setStyleSheetOpen) = rememberSaveable { mutableStateOf(false) }

    TopAppBar(
        scrollBehavior = scrollBehavior,
        navigationIcon = {
            ArticleNavigationIcon(
                onClick = onClose
            )
        },
        title = {},
        actions = {
            Row {
                if (article != null) {
                    TopBarTooltip(
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
                    TopBarTooltip(
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
                    TopBarTooltip(
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
                    TopBarTooltip(
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
                    TopBarTooltip(
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
//    ERROR -> R.drawable.
    else -> R.drawable.icon_article_empty
}

@Composable
fun ArticleNavigationIcon(onClick: () -> Unit) {
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

@Preview
@Composable
private fun ArticleTopBarPreview(@PreviewParameter(ArticleSample::class) article: Article) {
    InjectedCapyTheme {
        ArticleTopBar(
            article = article.copy(extractedContentURL = URL("https://example.com")),
            scrollBehavior = enterAlwaysScrollBehavior(),
            onToggleExtractContent = {},
            onToggleRead = {},
            onToggleStar = {},
            onClose = {}
        )
    }
}

@Preview(device = "id:pixel_fold")
@Composable
private fun ArticleTopBarPreview_Tablet(@PreviewParameter(ArticleSample::class) article: Article) {
    InjectedCapyTheme {
        ArticleTopBar(
            article = article,
            scrollBehavior = enterAlwaysScrollBehavior(),
            onToggleExtractContent = {},
            onToggleRead = {},
            onToggleStar = {},
            onClose = {}
        )
    }
}

@Preview
@Composable
private fun ArticleTopBarPreview_MissingArticle() {
    InjectedCapyTheme {
        ArticleTopBar(
            article = null,
            scrollBehavior = enterAlwaysScrollBehavior(),
            onToggleExtractContent = {},
            onToggleRead = {},
            onToggleStar = {},
            onClose = {}
        )
    }
}
