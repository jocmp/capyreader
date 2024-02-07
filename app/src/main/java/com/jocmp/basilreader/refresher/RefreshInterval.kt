package com.jocmp.basilreader.refresher

enum class RefreshInterval {
    MANUALLY_ONLY,
    EVERY_FIFTEEN_MINUTES,
    EVERY_THIRTY_MINUTES,
    EVERY_HOUR,
    EVERY_TWO_HOURS,
    EVERY_FOUR_HOURS,
    EVERY_EIGHT_HOURS;

    companion object {
        val default = MANUALLY_ONLY
    }
}
