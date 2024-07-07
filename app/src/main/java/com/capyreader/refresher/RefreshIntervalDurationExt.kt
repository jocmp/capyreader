package com.capyreader.refresher

import com.capyreader.refresher.RefreshInterval.EVERY_FIFTEEN_MINUTES
import com.capyreader.refresher.RefreshInterval.EVERY_HOUR
import com.capyreader.refresher.RefreshInterval.EVERY_THIRTY_MINUTES
import com.capyreader.refresher.RefreshInterval.MANUALLY_ONLY
import java.util.concurrent.TimeUnit

val RefreshInterval.toTime: Pair<Long, TimeUnit>?
    get() = when (this) {
        EVERY_FIFTEEN_MINUTES -> Pair(15, TimeUnit.MINUTES)
        EVERY_THIRTY_MINUTES -> Pair(30, TimeUnit.MINUTES)
        EVERY_HOUR -> Pair(1, TimeUnit.HOURS)
        MANUALLY_ONLY -> null
    }
