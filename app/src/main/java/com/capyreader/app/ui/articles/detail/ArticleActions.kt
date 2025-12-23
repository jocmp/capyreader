package com.capyreader.app.ui.articles.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
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
import androidx.compose.ui.unit.dp
import com.capyreader.app.R
import com.capyreader.app.common.shareArticle
import com.capyreader.app.ui.articles.FullContentLoadingIcon
import com.capyreader.app.ui.articles.LocalLabelsActions
import com.capyreader.app.ui.components.ToolbarTooltip
import com.jocmp.capy.Article
import com.jocmp.capy.Article.FullContentState.LOADED
import com.jocmp.capy.Article.FullContentState.LOADING

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleActions(
    article: Article,
    onToggleExtractContent: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
) {
    val context = LocalContext.current
    val labelsActions = LocalLabelsActions.current
    val (isStyleSheetOpen, setStyleSheetOpen) = rememberSaveable { mutableStateOf(false) }

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
    if (labelsActions.showLabels) {
        ToolbarTooltip(
            message = stringResource(R.string.freshrss_article_actions_label)
        ) {
            IconButton(
                onClick = { labelsActions.openSheet(article.id) },
            ) {
                Icon(
                    Icons.AutoMirrored.Outlined.Label,
                    contentDescription = stringResource(R.string.freshrss_article_actions_label)
                )
            }
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


    if (isStyleSheetOpen) {
        ModalBottomSheet(onDismissRequest = { setStyleSheetOpen(false) }) {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
            ) {
                ArticleStylePicker()
            }
        }
    }
}

@Composable
private fun readIcon(article: Article) =
    if (article.read) {
        Icons.Outlined.Circle
    } else {
        Icons.Rounded.Circle
    }

@Composable
private fun starredIcon(article: Article) =
    if (article.starred) {
        Icons.Rounded.Star
    } else {
        Icons.Rounded.StarOutline
    }

@Composable
private fun extractIcon(fullContentState: Article.FullContentState) = when (fullContentState) {
    LOADED -> R.drawable.icon_article_filled
    Article.FullContentState.ERROR -> R.drawable.icon_article_error
    else -> R.drawable.icon_article_empty
}
