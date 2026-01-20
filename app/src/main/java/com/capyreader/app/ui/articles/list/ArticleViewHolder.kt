package com.capyreader.app.ui.articles.list

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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

    private var article by mutableStateOf<Article?>(null)
    private var index by mutableIntStateOf(0)
    private var context by mutableStateOf<ArticleCompositionContext?>(null)

    init {
        composeView.setViewCompositionStrategy(
            ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed
        )
        composeView.setContent {
            val currentContext = context ?: return@setContent
            val currentArticle = article

            CapyTheme(appTheme = currentContext.appTheme) {
                CompositionLocalProvider(
                    LocalArticleActions provides currentContext.articleActions,
                    LocalLabelsActions provides currentContext.labelsActions,
                    LocalLinkOpener provides currentContext.linkOpener,
                    LocalAppTheme provides currentContext.appTheme,
                ) {
                    if (currentArticle == null) {
                        PlaceholderArticleRow(currentContext.options.imagePreview)
                    } else {
                        ArticleRow(
                            article = currentArticle,
                            index = index,
                            selected = currentContext.selectedArticleKey == currentArticle.id,
                            onSelect = onSelect,
                            onOpenMenu = onOpenMenu,
                            currentTime = currentContext.currentTime,
                            options = currentContext.options
                        )
                    }
                }
            }
        }
    }

    fun bind(
        article: Article?,
        index: Int,
        context: ArticleCompositionContext
    ) {
        this.article = article
        this.index = index
        this.context = context
    }
}
