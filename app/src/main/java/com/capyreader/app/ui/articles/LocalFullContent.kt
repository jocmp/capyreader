package com.capyreader.app.ui.articles

import androidx.compose.runtime.compositionLocalOf
import com.jocmp.capy.FullContent

val LocalFullContent = compositionLocalOf { FullContentFetcher() }

data class FullContentFetcher(
    val value: FullContent = FullContent.None,
    val fetch: () -> Unit = {},
    val reset: () -> Unit = {}
)
