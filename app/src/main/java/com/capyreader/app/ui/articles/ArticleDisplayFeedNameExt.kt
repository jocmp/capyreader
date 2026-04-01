package com.capyreader.app.ui.articles

import android.content.Context
import com.capyreader.app.R
import com.jocmp.capy.Article

fun Article.displayFeedName(context: Context): String {
    return if (isReadLater) {
        context.getString(R.string.filter_read_later)
    } else {
        feedName
    }
}
