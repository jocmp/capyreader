package com.capyreader.app.ui.articles.list

import androidx.compose.runtime.Composable
import androidx.glance.text.Text
import org.koin.androidx.compose.koinViewModel

@Composable
fun ArticleListDetailLayout(viewModel: ArticleListDetailViewModel = koinViewModel()) {
    Text(viewModel.filter.toString())
}
