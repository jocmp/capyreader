package com.capyreader.app.ui.articles.detail

import android.view.HapticFeedbackConstants
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.rounded.Circle
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.TooltipAnchorPosition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.capyreader.app.R
import com.capyreader.app.common.shareArticle
import com.capyreader.app.ui.articles.FullContentLoadingIcon
import com.capyreader.app.ui.components.ToolbarTooltip
import com.jocmp.capy.Article
import com.jocmp.capy.Article.FullContentState.LOADED
import com.jocmp.capy.Article.FullContentState.LOADING

private val sizeSpec = spring<IntSize>(stiffness = 700f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleBottomBar(
    show: Boolean,
    article: Article,
    hasNextArticle: Boolean,
    onToggleExtractContent: () -> Unit,
    onToggleRead: () -> Unit,
    onToggleStar: () -> Unit,
    onSelectNext: () -> Unit,
) {
    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f),
        contentAlignment = Alignment.BottomCenter
    ) {
        AnimatedVisibility(
            visible = show,
            enter = expandVertically(expandFrom = Alignment.Top, animationSpec = sizeSpec),
            exit = shrinkVertically(shrinkTowards = Alignment.Top, animationSpec = sizeSpec)
        ) {
            val view = LocalView.current

            Surface(
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Row(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .fillMaxWidth()
                        .height(60.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ToolbarTooltip(
                        positioning = TooltipAnchorPosition.Above,
                        message = stringResource(R.string.article_view_mark_as_read)
                    ) {
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onToggleRead()
                            },
                        ) {
                            Icon(
                                if (article.read) Icons.Outlined.Circle else Icons.Rounded.Circle,
                                contentDescription = stringResource(R.string.article_view_mark_as_read),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    ToolbarTooltip(
                        positioning = TooltipAnchorPosition.Above,
                        message = stringResource(R.string.article_view_star)
                    ) {
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onToggleStar()
                            },
                        ) {
                            Icon(
                                if (article.starred) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                                contentDescription = stringResource(R.string.article_view_star),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    ToolbarTooltip(
                        positioning = TooltipAnchorPosition.Above,
                        message = stringResource(R.string.article_bottom_bar_next_article)
                    ) {
                        IconButton(
                            enabled = hasNextArticle,
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onSelectNext()
                            },
                        ) {
                            Icon(
                                Icons.Rounded.ExpandMore,
                                contentDescription = stringResource(R.string.article_bottom_bar_next_article),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    ToolbarTooltip(
                        positioning = TooltipAnchorPosition.Above,
                        message = stringResource(R.string.extract_full_content)
                    ) {
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                onToggleExtractContent()
                            },
                        ) {
                            if (article.fullContent == LOADING) {
                                FullContentLoadingIcon()
                            } else {
                                Icon(
                                    painterResource(id = extractIcon(article.fullContent)),
                                    contentDescription = stringResource(R.string.extract_full_content),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    }
                    ToolbarTooltip(
                        positioning = TooltipAnchorPosition.Above,
                        message = stringResource(R.string.article_share)
                    ) {
                        IconButton(
                            onClick = {
                                view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP)
                                context.shareArticle(article = article)
                            },
                        ) {
                            Icon(
                                Icons.Rounded.Share,
                                contentDescription = stringResource(R.string.article_share),
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun extractIcon(fullContentState: Article.FullContentState) = when (fullContentState) {
    LOADED -> R.drawable.icon_article_filled
    Article.FullContentState.ERROR -> R.drawable.icon_article_error
    else -> R.drawable.icon_article_empty
}
