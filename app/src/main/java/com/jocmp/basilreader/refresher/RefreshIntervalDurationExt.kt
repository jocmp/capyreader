package com.jocmp.basilreader.refresher

import com.jocmp.basilreader.refresher.RefreshInterval.EVERY_EIGHT_HOURS
import com.jocmp.basilreader.refresher.RefreshInterval.EVERY_FIFTEEN_MINUTES
import com.jocmp.basilreader.refresher.RefreshInterval.EVERY_FOUR_HOURS
import com.jocmp.basilreader.refresher.RefreshInterval.EVERY_HOUR
import com.jocmp.basilreader.refresher.RefreshInterval.EVERY_THIRTY_MINUTES
import com.jocmp.basilreader.refresher.RefreshInterval.EVERY_TWO_HOURS
import com.jocmp.basilreader.refresher.RefreshInterval.MANUALLY_ONLY
import java.util.concurrent.TimeUnit

val RefreshInterval.toTime: Pair<Long, TimeUnit>?
    get() = when (this) {
        EVERY_FIFTEEN_MINUTES -> Pair(15, TimeUnit.MINUTES)
        EVERY_THIRTY_MINUTES -> Pair(30, TimeUnit.MINUTES)
        EVERY_HOUR -> Pair(1, TimeUnit.HOURS)
        EVERY_TWO_HOURS -> Pair(2, TimeUnit.HOURS)
        EVERY_FOUR_HOURS -> Pair(4, TimeUnit.HOURS)
        EVERY_EIGHT_HOURS -> Pair(8, TimeUnit.HOURS)
        MANUALLY_ONLY -> null
    }
