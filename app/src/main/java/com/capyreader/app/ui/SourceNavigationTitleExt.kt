package com.capyreader.app.ui

import com.capyreader.app.R
import com.jocmp.capy.accounts.Source

val Source.savedSearchNavTitle: Int
    get() = if (this == Source.FRESHRSS) {
        R.string.freshrss_nav_headline_my_labels
    } else {
        R.string.nav_headline_saved_searches
    }

val Source.folderNavTitle: Int
    get() = if (this == Source.FRESHRSS) {
        R.string.freshrss_nav_headline_categories
    } else {
        R.string.nav_headline_tags
    }
