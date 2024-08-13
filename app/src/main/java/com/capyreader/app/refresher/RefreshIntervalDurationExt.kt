package com.capyreader.app.refresher

import com.capyreader.app.refresher.RefreshInterval.*
import java.util.concurrent.TimeUnit

val RefreshInterval.toTime: Pair<Long, TimeUnit>?
    get() = when (this) {
        EVERY_FIFTEEN_MINUTES -> Pair(15, TimeUnit.MINUTES)
        EVERY_THIRTY_MINUTES -> Pair(30, TimeUnit.MINUTES)
        EVERY_HOUR -> Pair(1, TimeUnit.HOURS)
        EVERY_12_HOURS -> Pair(12, TimeUnit.HOURS)
        EVERY_DAY -> Pair(24, TimeUnit.HOURS)
        ON_START -> null
        MANUALLY_ONLY -> null
    }
