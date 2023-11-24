package com.jocmp.basilreader.ui.articles

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import org.koin.androidx.compose.koinViewModel

@Composable
fun ArticlesView(
    viewModel: ArticlesViewModel = koinViewModel()
) {
    Text("read me: ${viewModel.account.id}")
}
