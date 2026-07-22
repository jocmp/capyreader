package com.capyreader.app.ui.articles.list

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.compose.LazyPagingItems
import com.capyreader.app.R
import com.capyreader.app.ui.articles.ArticleList
import com.capyreader.app.ui.components.ArticleSearch
import com.capyreader.app.ui.components.SearchTextField
import com.jocmp.capy.Article

/**
 * Full-surface search overlay. It owns its own results pager so the list (and therefore the
 * reader's neighbor query) underneath keeps the base filter untouched. Shown while
 * [ArticleSearch.isActive]; search stays active when a result is selected so two-pane
 * layouts keep the results visible next to the reader.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchView(
    search: ArticleSearch,
    results: LazyPagingItems<Article>,
    selectedArticleID: String?,
    dimReadArticles: Boolean,
    onSelect: (article: Article) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val query = search.query.orEmpty()

    Surface(
        modifier = Modifier
            .fillMaxSize()
            // Eat taps that land on gaps so they can't fall through to the list beneath the overlay.
            .pointerInput(Unit) { detectTapGestures { } }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    navigationIcon = {
                        IconButton(onClick = search.clear) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                                contentDescription = stringResource(R.string.feed_list_top_bar_close_search)
                            )
                        }
                    },
                    title = {
                        SearchTextField(
                            placeholder = { Text(stringResource(R.string.search_bar_placeholder)) },
                            value = query,
                            onValueChange = search.update,
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                            ),
                            trailingIcon = {
                                if (query.isNotEmpty()) {
                                    IconButton(onClick = { search.update("") }) {
                                        Icon(
                                            imageVector = Icons.Rounded.Close,
                                            contentDescription = null
                                        )
                                    }
                                }
                            },
                            singleLine = true,
                            maxLines = 1,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .focusRequester(focusRequester),
                        )
                    },
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            ) {
                if (query.isNotBlank() && results.itemCount == 0) {
                    Text(
                        text = stringResource(R.string.search_no_results),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 32.dp)
                    )
                } else {
                    ArticleList(
                        articles = results,
                        selectedArticleKey = selectedArticleID,
                        listState = rememberLazyListState(),
                        dimReadArticles = dimReadArticles,
                        onSelect = onSelect,
                    )
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        if (query.isBlank()) {
            focusRequester.requestFocus()
        }
    }
}
