package com.capyreader.app.ui.articles.detail

sealed class ArticleShortcut {
    data object NextArticle : ArticleShortcut()
    data object PreviousArticle : ArticleShortcut()
    data object OpenInBrowser : ArticleShortcut()
    data object ToggleStar : ArticleShortcut()
    data object ToggleRead : ArticleShortcut()
    data object ToggleFullContent : ArticleShortcut()
    data object GoBack : ArticleShortcut()
    data object ScrollDown : ArticleShortcut()
    data object ScrollUp : ArticleShortcut()
    data object FocusList : ArticleShortcut()
    data object FocusDetail : ArticleShortcut()
}
