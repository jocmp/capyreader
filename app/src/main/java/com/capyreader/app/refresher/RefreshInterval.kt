package com.capyreader.app.refresher

enum class RefreshInterval {
    MANUALLY_ONLY,
    EVERY_FIFTEEN_MINUTES,
    EVERY_THIRTY_MINUTES,
    EVERY_HOUR,
    EVERY_TWO_HOURS,
    EVERY_12_HOURS,
    EVERY_DAY;

    val isPeriodic: Boolean
        get() = this != MANUALLY_ONLY

    companion object {
        val default = EVERY_TWO_HOURS
    }
}
