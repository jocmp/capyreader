package com.capyreader.app.ui.articles.detail

import android.view.ViewGroup
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.findViewTreeViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.recyclerview.widget.RecyclerView
import androidx.savedstate.findViewTreeSavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.capyreader.app.ui.articles.list.ArticlePagingAdapter
import com.jocmp.capy.Article

class ArticlePagerAdapter(
    private val articleAdapter: ArticlePagingAdapter,
    private val content: @Composable (Article) -> Unit,
) : RecyclerView.Adapter<ArticlePagerAdapter.ViewHolder>() {

    class ViewHolder(val composeView: ComposeView) : RecyclerView.ViewHolder(composeView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val composeView = ComposeView(parent.context).apply {
            layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            setViewTreeLifecycleOwner(parent.findViewTreeLifecycleOwner())
            setViewTreeViewModelStoreOwner(parent.findViewTreeViewModelStoreOwner())
            setViewTreeSavedStateRegistryOwner(parent.findViewTreeSavedStateRegistryOwner())
        }
        return ViewHolder(composeView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val article = articleAdapter.getArticle(position) ?: return
        holder.composeView.setContent {
            content(article)
        }
    }

    override fun getItemCount(): Int = articleAdapter.itemCount

    override fun getItemId(position: Int): Long {
        return articleAdapter.getArticle(position)?.id?.hashCode()?.toLong() ?: position.toLong()
    }

    fun findPositionForArticle(articleId: String): Int {
        for (i in 0 until itemCount) {
            if (articleAdapter.getArticle(i)?.id == articleId) {
                return i
            }
        }
        return -1
    }
}
