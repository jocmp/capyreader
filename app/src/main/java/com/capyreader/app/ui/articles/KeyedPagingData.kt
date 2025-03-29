package com.capyreader.app.ui.articles

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

data class KeyedPagingData<T: Any>(
    val key: String = "",
    val value: Flow<PagingData<T>> = flowOf()
)
