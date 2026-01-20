package com.capyreader.app.ui.articles.list

import com.capyreader.app.preferences.AppTheme
import com.capyreader.app.preferences.RowSwipeOption
import com.capyreader.app.preferences.ThemeMode
import com.capyreader.app.ui.LinkOpener
import com.capyreader.app.ui.articles.ArticleActions
import com.capyreader.app.ui.articles.ArticleRowOptions
import com.capyreader.app.ui.articles.LabelsActions
import java.time.LocalDateTime

data class ArticleCompositionContext(
    val articleActions: ArticleActions,
    val labelsActions: LabelsActions,
    val linkOpener: LinkOpener,
    val appTheme: AppTheme,
    val themeMode: ThemeMode,
    val pureBlackDarkMode: Boolean,
    val options: ArticleRowOptions,
    val currentTime: LocalDateTime,
    val swipeStart: RowSwipeOption,
    val swipeEnd: RowSwipeOption,
    val selectedArticleKey: String?,
)
