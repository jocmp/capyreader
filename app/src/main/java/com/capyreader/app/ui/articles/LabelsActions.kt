package com.capyreader.app.ui.articles

import androidx.compose.runtime.Stable
import androidx.compose.runtime.compositionLocalOf
import com.jocmp.capy.SavedSearch
import com.jocmp.capy.accounts.Source

val LocalLabelsActions = compositionLocalOf { LabelsActions() }

@Stable
data class LabelsActions(
    val source: Source = Source.LOCAL,
    val showLabels: Boolean = false,
    val savedSearches: List<SavedSearch> = emptyList(),
    val selectedArticleID: String? = null,
    val articleLabels: List<String> = emptyList(),
    val openSheet: (articleID: String) -> Unit = {},
    val closeSheet: () -> Unit = {},
    val addLabel: (articleID: String, savedSearchID: String) -> Unit = { _, _ -> },
    val removeLabel: (articleID: String, savedSearchID: String) -> Unit = { _, _ -> },
    val createLabel: suspend (articleID: String, name: String) -> Result<String> = { _, _ -> Result.success("") },
)
