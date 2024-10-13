package com.capyreader.app.refresher

enum class RefreshInterval {
    MANUALLY_ONLY,
    ON_START,
    EVERY_FIFTEEN_MINUTES,
    EVERY_THIRTY_MINUTES,
    EVERY_HOUR,
    EVERY_12_HOURS,
    EVERY_DAY;

    val isPeriodic: Boolean
        get() = !(this == MANUALLY_ONLY || this == ON_START)

    companion object {
        val default = ON_START
    }
}
