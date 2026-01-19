package com.capyreader.app.ui.articles.list

import androidx.recyclerview.widget.DiffUtil
import com.jocmp.capy.Article

object ArticleDiffCallback : DiffUtil.ItemCallback<Article>() {
    override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem == newItem
    }

    override fun getChangePayload(oldItem: Article, newItem: Article): Any? {
        return if (oldItem.read != newItem.read || oldItem.starred != newItem.starred) {
            ArticleChangePayload(
                readChanged = oldItem.read != newItem.read,
                starredChanged = oldItem.starred != newItem.starred
            )
        } else {
            null
        }
    }
}

data class ArticleChangePayload(
    val readChanged: Boolean,
    val starredChanged: Boolean
)
