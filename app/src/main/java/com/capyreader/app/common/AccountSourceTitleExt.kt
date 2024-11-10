package com.capyreader.app.common

import com.capyreader.app.R
import com.jocmp.capy.accounts.Source

val Source.titleKey: Int
    get() = when (this) {
        Source.FEEDBIN -> R.string.account_source_feedbin
        Source.FRESHRSS -> R.string.account_source_freshrss
        Source.LOCAL -> R.string.account_source_local
        Source.READER -> R.string.account_source_reader
    }
