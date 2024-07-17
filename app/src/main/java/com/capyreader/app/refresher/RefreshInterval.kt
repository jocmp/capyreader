package com.capyreader.app.refresher

enum class RefreshInterval {
    MANUALLY_ONLY,
    ON_START,
    EVERY_FIFTEEN_MINUTES,
    EVERY_THIRTY_MINUTES,
    EVERY_HOUR;

    companion object {
        val default = ON_START
    }
}
