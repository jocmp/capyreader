package com.capyreader.app.ui.articles.detail

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.paging.LoadState
import androidx.viewpager2.widget.ViewPager2
import com.capyreader.app.ui.articles.list.ArticlePagingAdapter
import com.jocmp.capy.Article
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filter

@Composable
fun ArticleViewPager(
    article: Article,
    articleAdapter: ArticlePagingAdapter,
    onArticleChange: (Article) -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (Article) -> Unit,
) {
    var currentPosition by remember { mutableIntStateOf(-1) }
    var viewPagerRef by remember { mutableStateOf<ViewPager2?>(null) }
    var articleFound by remember { mutableStateOf(articleAdapter.findPositionForArticle(article.id) >= 0) }
    val currentArticle by rememberUpdatedState(article)

    val pagerAdapter = remember(articleAdapter) {
        ArticlePagerAdapter(articleAdapter, content).apply {
            setHasStableIds(true)
        }
    }

    // Sync position when article changes externally
    LaunchedEffect(article.id) {
        val targetPosition = articleAdapter.findPositionForArticle(article.id)
        if (targetPosition >= 0) {
            articleFound = true
            if (targetPosition != currentPosition) {
                viewPagerRef?.setCurrentItem(targetPosition, currentPosition >= 0)
                currentPosition = targetPosition
            }
        } else {
            articleFound = false
            currentPosition = -1
        }
    }

    // Refresh pager when data loads and try to find article
    LaunchedEffect(articleAdapter, pagerAdapter) {
        articleAdapter.loadStateFlow
            .filter { it.refresh is LoadState.NotLoading && articleAdapter.itemCount > 0 }
            .collectLatest {
                pagerAdapter.notifyDataSetChanged()
                val targetPosition = articleAdapter.findPositionForArticle(currentArticle.id)
                if (targetPosition >= 0) {
                    articleFound = true
                    if (targetPosition != currentPosition) {
                        viewPagerRef?.setCurrentItem(targetPosition, false)
                        currentPosition = targetPosition
                    }
                }
            }
    }

    // Show article directly until it's found in the adapter
    if (!articleFound) {
        content(article)
        return
    }

    AndroidView(
        modifier = modifier,
        factory = { context ->
            ViewPager2(context).apply {
                adapter = pagerAdapter

                registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                    override fun onPageSelected(position: Int) {
                        if (position != currentPosition) {
                            currentPosition = position
                            articleAdapter.getArticle(position)?.let { newArticle ->
                                if (newArticle.id != article.id) {
                                    onArticleChange(newArticle)
                                }
                            }
                        }
                    }
                })

                // Set initial position if data is already available
                val initialPosition = pagerAdapter.findPositionForArticle(article.id)
                if (initialPosition >= 0) {
                    setCurrentItem(initialPosition, false)
                    currentPosition = initialPosition
                }

                viewPagerRef = this
            }
        },
        update = { pager ->
            viewPagerRef = pager
        }
    )
}
