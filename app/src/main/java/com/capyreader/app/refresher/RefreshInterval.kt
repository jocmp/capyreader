package com.capyreader.app.refresher

enum class RefreshInterval {
    MANUALLY_ONLY,
    ON_START,
    EVERY_THIRTY_MINUTES,
    EVERY_HOUR,
    EVERY_TWO_HOURS,
    EVERY_FOUR_HOURS,
    EVERY_EIGHT_HOURS;

    val isPeriodic: Boolean
        get() = !(this == MANUALLY_ONLY || this == ON_START)

    companion object {
        val default = EVERY_TWO_HOURS
    }
}
