package com.capyreader.app.ui.articles.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Label
import androidx.compose.material.icons.outlined.FormatSize
import androidx.compose.material.icons.rounded.Print
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.capyreader.app.R
import com.capyreader.app.common.printArticle
import com.capyreader.app.ui.articles.LocalLabelsActions
import com.capyreader.app.ui.components.ToolbarTooltip
import com.capyreader.app.ui.fixtures.PreviewKoinApplication
import com.jocmp.capy.Article

private val sizeSpec = spring<IntSize>(stiffness = 700f)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleTopBar(
    show: Boolean,
    isScrolled: Boolean,
    article: Article? = null,
    onClose: () -> Unit,
) {
    val containerColor = MaterialTheme.colorScheme.surface
    val labelsActions = LocalLabelsActions.current
    val context = LocalContext.current
    val (isStyleSheetOpen, setStyleSheetOpen) = rememberSaveable { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .zIndex(1f),
        contentAlignment = Alignment.TopCenter,
    ) {
        Column(modifier = Modifier.drawBehind { drawRect(containerColor) }) {
            Spacer(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(WindowInsets.statusBars.asPaddingValues().calculateTopPadding())
            )
            AnimatedVisibility(
                visible = show,
                enter = expandVertically(
                    expandFrom = Alignment.Bottom,
                    animationSpec = sizeSpec
                ),
                exit = shrinkVertically(
                    shrinkTowards = Alignment.Bottom,
                    animationSpec = sizeSpec
                ),
            ) {
                TopAppBar(
                    navigationIcon = {
                        ArticleNavigationIcon(
                            onClick = onClose
                        )
                    },
                    title = {},
                    actions = {
                        if (labelsActions.showLabels && article != null) {
                            ToolbarTooltip(
                                message = stringResource(R.string.freshrss_article_actions_label)
                            ) {
                                IconButton(
                                    onClick = { labelsActions.openSheet(article.id) },
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Outlined.Label,
                                        contentDescription = stringResource(R.string.freshrss_article_actions_label),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        }
                        if (article != null) {
                            ToolbarTooltip(
                                message = stringResource(R.string.article_print)
                            ) {
                                IconButton(
                                    onClick = { context.printArticle(article = article) },
                                ) {
                                    Icon(
                                        Icons.Rounded.Print,
                                        contentDescription = stringResource(R.string.article_print),
                                        modifier = Modifier.size(24.dp)
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
                                    contentDescription = stringResource(R.string.article_style_options),
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    },
                    windowInsets = WindowInsets(0.dp),
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                    ),
                )
            }
            if (isScrolled) {
                HorizontalDivider(
                    color = MaterialTheme.colorScheme.surfaceContainerHighest,
                    thickness = 0.5f.dp,
                )
            }
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

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun ArticleTopBarPreview() {
    PreviewKoinApplication {
        ArticleTopBar(
            show = true,
            isScrolled = false,
            onClose = {}
        )
    }
}
