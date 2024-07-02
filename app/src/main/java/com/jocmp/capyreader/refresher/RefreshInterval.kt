package com.jocmp.capyreader.refresher

enum class RefreshInterval {
    MANUALLY_ONLY,
    EVERY_FIFTEEN_MINUTES,
    EVERY_THIRTY_MINUTES,
    EVERY_HOUR;

    companion object {
        val default = MANUALLY_ONLY
    }
}
