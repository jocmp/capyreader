package com.capyreader.app.ui.articles.list

import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.InsetDrawable
import android.view.LayoutInflater
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.paging.PagingData
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.capyreader.app.R
import com.capyreader.app.common.asState
import com.capyreader.app.preferences.AppPreferences
import com.capyreader.app.ui.LocalLinkOpener
import com.capyreader.app.ui.LocalUnreadCount
import com.capyreader.app.ui.articles.ArticleMenuState
import com.capyreader.app.ui.articles.LocalArticleActions
import com.capyreader.app.ui.articles.LocalLabelsActions
import com.capyreader.app.ui.articles.rememberArticleOptions
import com.capyreader.app.ui.articles.rememberCurrentTime
import com.capyreader.app.ui.theme.LocalAppTheme
import com.jocmp.capy.Article
import com.jocmp.capy.MarkRead
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import org.koin.compose.koinInject

@OptIn(FlowPreview::class)
@Composable
fun ArticleRecyclerView(
    articles: Flow<PagingData<Article>>,
    listKey: String,
    selectedArticleKey: String?,
    onSelect: (articleID: String) -> Unit,
    onMarkAllRead: (range: MarkRead) -> Unit,
    enableMarkReadOnScroll: Boolean,
    refreshingAll: Boolean,
    scrollState: ArticleListScrollState,
    appPreferences: AppPreferences = koinInject(),
) {
    val articleActions = LocalArticleActions.current
    val labelsActions = LocalLabelsActions.current
    val linkOpener = LocalLinkOpener.current
    val appTheme = LocalAppTheme.current
    val options = rememberArticleOptions()
    val currentTime = rememberCurrentTime()
    val lifecycleOwner = LocalLifecycleOwner.current
    val unreadCount = LocalUnreadCount.current

    val swipeStart by appPreferences.articleListOptions.swipeStart.asState()
    val swipeEnd by appPreferences.articleListOptions.swipeEnd.asState()
    val themeMode by appPreferences.themeMode.asState()
    val pureBlackDarkMode by appPreferences.pureBlackDarkMode.asState()

    val (menuState, setMenuState) = remember { mutableStateOf<ArticleMenuState?>(null) }
    val scrollbarColor = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    val compositionContext = remember(
        articleActions,
        labelsActions,
        linkOpener,
        appTheme,
        themeMode,
        pureBlackDarkMode,
        options,
        currentTime,
        swipeStart,
        swipeEnd,
        selectedArticleKey
    ) {
        ArticleCompositionContext(
            articleActions = articleActions,
            labelsActions = labelsActions,
            linkOpener = linkOpener,
            appTheme = appTheme,
            themeMode = themeMode,
            pureBlackDarkMode = pureBlackDarkMode,
            options = options,
            currentTime = currentTime,
            swipeStart = swipeStart,
            swipeEnd = swipeEnd,
            selectedArticleKey = selectedArticleKey
        )
    }

    val adapter = remember {
        ArticlePagingAdapter(
            onSelect = onSelect,
            onOpenMenu = { state -> setMenuState(state) },
        )
    }

    SideEffect {
        adapter.compositionContext = compositionContext
    }

    LaunchedEffect(Unit) {
        articles.collectLatest { pagingData ->
            adapter.submitData(pagingData)
        }
    }

    val itemTouchHelper = remember(adapter) {
        ItemTouchHelper(
            ArticleItemTouchHelper(
                adapter = adapter,
                getSwipeStart = { swipeStart },
                getSwipeEnd = { swipeEnd },
                articleActions = { articleActions },
                openLink = { uri -> linkOpener.open(uri) }
            )
        )
    }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            val recyclerView = LayoutInflater.from(context)
                .inflate(R.layout.article_recycler_view, null) as RecyclerView
            recyclerView.apply {
                val linearLayoutManager = LinearLayoutManager(context)
                layoutManager = linearLayoutManager
                this.adapter = adapter
                itemTouchHelper.attachToRecyclerView(this)

                setScrollbarThumbColor(scrollbarColor)

                addOnScrollListener(object : RecyclerView.OnScrollListener() {
                    override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                        val layoutManager =
                            recyclerView.layoutManager as? LinearLayoutManager ?: return
                        scrollState.updateFromLayoutManager(layoutManager)
                    }
                })
            }
        },
        update = { recyclerView ->
            scrollState.recyclerView = recyclerView
        },
    )

    menuState?.let { state ->
        Box(
            modifier = Modifier.offset {
                IntOffset(0, state.yPosition)
            }
        ) {
            ArticleActionMenu(
                expanded = true,
                article = state.article,
                index = state.index,
                unreadCount = unreadCount,
                articleActions = articleActions,
                showLabels = labelsActions.showLabels,
                onMarkAllRead = {
                    setMenuState(null)
                    onMarkAllRead(it)
                },
                onOpenLabels = {
                    setMenuState(null)
                    labelsActions.openSheet(state.article.id)
                },
                onDismissRequest = {
                    setMenuState(null)
                }
            )
        }
    }

    if (enableMarkReadOnScroll && !refreshingAll) {
        LaunchedEffect(scrollState, adapter) {
            snapshotFlow { scrollState.firstVisibleItemIndex }
                .debounce(500)
                .distinctUntilChanged()
                .collect { firstVisibleIndex ->
                    val offscreenIndex = firstVisibleIndex - 1

                    val markAsRead =
                        (adapter.itemCount == 1 && firstVisibleIndex > 0) ||
                                (offscreenIndex > 0 && adapter.itemCount > 0)

                    if (!markAsRead) {
                        return@collect
                    }

                    val item = adapter.getArticle(offscreenIndex)

                    item?.let { onMarkAllRead(MarkRead.After(it.id)) }
                }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_DESTROY) {
                scrollState.recyclerView = null
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
}

class ArticleListScrollState {
    var recyclerView: RecyclerView? = null
    var firstVisibleItemIndex: Int by mutableIntStateOf(0)
        private set
    var firstVisibleItemScrollOffset: Int by mutableIntStateOf(0)
        private set

    fun updateFromLayoutManager(layoutManager: LinearLayoutManager) {
        firstVisibleItemIndex = layoutManager.findFirstVisibleItemPosition()
        val firstVisibleView = layoutManager.findViewByPosition(firstVisibleItemIndex)
        firstVisibleItemScrollOffset = firstVisibleView?.top ?: 0
    }

    fun scrollToItem(index: Int) {
        recyclerView?.scrollToPosition(index)
    }

    fun animateScrollToItem(index: Int) {
        recyclerView?.smoothScrollToPosition(index)
    }

    val layoutInfo: ArticleListLayoutInfo
        get() {
            val rv = recyclerView
            val lm = rv?.layoutManager as? LinearLayoutManager
            return if (rv != null && lm != null) {
                val visibleItemsInfo =
                    (lm.findFirstVisibleItemPosition()..lm.findLastVisibleItemPosition())
                        .mapNotNull { pos ->
                            lm.findViewByPosition(pos)?.let { view ->
                                ArticleVisibleItemInfo(index = pos, size = view.height)
                            }
                        }
                ArticleListLayoutInfo(
                    totalItemsCount = rv.adapter?.itemCount ?: 0,
                    visibleItemsInfo = visibleItemsInfo
                )
            } else {
                ArticleListLayoutInfo(totalItemsCount = 0, visibleItemsInfo = emptyList())
            }
        }
}

data class ArticleListLayoutInfo(
    val totalItemsCount: Int,
    val visibleItemsInfo: List<ArticleVisibleItemInfo>
)

data class ArticleVisibleItemInfo(
    val index: Int,
    val size: Int
)

@Composable
fun rememberArticleListScrollState(): ArticleListScrollState {
    return remember { ArticleListScrollState() }
}

private fun RecyclerView.setScrollbarThumbColor(color: Int) {
    val density = context.resources.displayMetrics.density
    val thumbWidthPx = (4 * density).toInt()
    val cornerRadiusPx = 100 * density
    val insetRightPx = (2 * density).toInt()

    val thumbShape = GradientDrawable().apply {
        shape = GradientDrawable.RECTANGLE
        setColor(color)
        cornerRadius = cornerRadiusPx
        setSize(thumbWidthPx, 0)
    }
    verticalScrollbarThumbDrawable = InsetDrawable(thumbShape, 0, 0, insetRightPx, 0)
}
