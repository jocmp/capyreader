package com.capyreader.app.ui.articles.list

import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.paging.PagingDataAdapter
import com.jocmp.capy.Article
import com.jocmp.capy.MarkRead

class ArticlePagingAdapter(
    private val onSelect: (articleID: String) -> Unit,
    private val onMarkAllRead: (range: MarkRead) -> Unit,
) : PagingDataAdapter<Article, ArticleViewHolder>(ArticleDiffCallback) {

    var compositionContext: ArticleCompositionContext? = null
        set(value) {
            field = value
            if (value != null) {
                notifyItemRangeChanged(0, itemCount, CONTEXT_PAYLOAD)
            }
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArticleViewHolder {
        val composeView = ComposeView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        return ArticleViewHolder(composeView, onSelect, onMarkAllRead)
    }

    override fun onBindViewHolder(holder: ArticleViewHolder, position: Int) {
        val context = compositionContext ?: return
        holder.bind(getItem(position), position, context)
    }

    override fun onBindViewHolder(
        holder: ArticleViewHolder,
        position: Int,
        payloads: MutableList<Any>
    ) {
        if (payloads.isEmpty() || payloads.any { it !is ArticleChangePayload && it != CONTEXT_PAYLOAD }) {
            onBindViewHolder(holder, position)
        } else {
            val context = compositionContext ?: return
            holder.bind(getItem(position), position, context)
        }
    }

    fun getArticle(position: Int): Article? {
        return if (position in 0 until itemCount) getItem(position) else null
    }

    companion object {
        private const val CONTEXT_PAYLOAD = "context_update"
    }
}
