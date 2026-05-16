package com.capyreader.lite.ui.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.capyreader.lite.R
import com.capyreader.lite.ui.feeds.FraidycatViewModel
import com.jocmp.capy.Article
import org.koin.compose.koinInject

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlePagerScreen(
    onBack: () -> Unit,
    viewModel: FraidycatViewModel = koinInject(),
) {
    val articles by viewModel.selectedArticles.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState(pageCount = { articles.size })

    LaunchedEffect(pagerState.currentPage, articles) {
        articles.getOrNull(pagerState.currentPage)?.let {
            // Follow read state but do not filter the view.
            if (!it.read) viewModel.markRead(it.id)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = articles
                            .getOrNull(pagerState.currentPage)
                            ?.title.orEmpty(),
                        maxLines = 1,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { padding ->
        if (articles.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center,
            ) {
                Text(stringResource(R.string.article_empty))
            }
            return@Scaffold
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) { page ->
            ArticlePage(article = articles[page])
        }
    }
}

@Composable
private fun ArticlePage(article: Article) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(text = article.title, style = MaterialTheme.typography.headlineSmall)
        article.author?.takeIf { it.isNotBlank() }?.let {
            Text(text = it, style = MaterialTheme.typography.labelMedium)
        }
        // TODO: render full content / sticky full content via WebView.
        Text(text = article.summary)
    }
}
