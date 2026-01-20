package com.capyreader.app.ui.articles.list

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.RecyclerView
import com.capyreader.app.ui.LocalLinkOpener
import com.capyreader.app.ui.articles.ArticleMenuState
import com.capyreader.app.ui.articles.ArticleRow
import com.capyreader.app.ui.articles.LocalArticleActions
import com.capyreader.app.ui.articles.LocalLabelsActions
import com.capyreader.app.ui.articles.PlaceholderArticleRow
import com.capyreader.app.ui.theme.CapyTheme
import com.capyreader.app.ui.theme.LocalAppTheme
import com.jocmp.capy.Article

class ArticleViewHolder(
    val composeView: ComposeView,
    private val onSelect: (articleID: String) -> Unit,
    private val onOpenMenu: (ArticleMenuState) -> Unit,
) : RecyclerView.ViewHolder(composeView) {

    init {
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
    }

    fun bind(
        article: Article?,
        index: Int,
        context: ArticleCompositionContext
    ) {
        composeView.setContent {
            CapyTheme(appTheme = context.appTheme) {
                CompositionLocalProvider(
                    LocalArticleActions provides context.articleActions,
                    LocalLabelsActions provides context.labelsActions,
                    LocalLinkOpener provides context.linkOpener,
                    LocalAppTheme provides context.appTheme,
                ) {
                    if (article == null) {
                        PlaceholderArticleRow(context.options.imagePreview)
                    } else {
                        ArticleRow(
                            article = article,
                            index = index,
                            selected = context.selectedArticleKey == article.id,
                            onSelect = onSelect,
                            onOpenMenu = onOpenMenu,
                            currentTime = context.currentTime,
                            options = context.options
                        )
                    }
                }
            }
        }
    }
}
