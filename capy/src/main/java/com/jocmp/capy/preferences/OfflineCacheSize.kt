package com.jocmp.capy.preferences

sealed class OfflineCacheSize(val limit: Int) {
    data object Off : OfflineCacheSize(0)
    data class Latest(val count: Int) : OfflineCacheSize(count) {
        init { require(count > 0) { "Latest cache size must be positive" } }
    }

    companion object {
        val default: OfflineCacheSize = Off

        val options: List<OfflineCacheSize> = listOf(
            Off,
            Latest(100),
            Latest(250),
            Latest(500),
        )

        fun fromLimit(value: Int): OfflineCacheSize =
            if (value <= 0) Off else Latest(value)
    }
}
