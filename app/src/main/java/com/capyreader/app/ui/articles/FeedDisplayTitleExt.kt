package com.capyreader.app.ui.articles

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.capyreader.app.R
import com.jocmp.capy.Feed

@Composable
fun Feed.displayTitle(): String {
    return if (isReadLater) {
        stringResource(R.string.filter_read_later)
    } else {
        title
    }
}
