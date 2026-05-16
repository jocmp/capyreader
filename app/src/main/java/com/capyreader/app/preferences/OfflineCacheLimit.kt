package com.capyreader.app.preferences

enum class OfflineCacheLimit(val bytes: Long) {
    MB_200(200L * 1024 * 1024),
    MB_500(500L * 1024 * 1024),
    GB_1(1024L * 1024 * 1024),
    GB_2(2L * 1024 * 1024 * 1024),
    UNLIMITED(OFFLINE_CACHE_UNLIMITED);

    companion object {
        val default: OfflineCacheLimit = MB_500

        fun forBytes(bytes: Long): OfflineCacheLimit =
            entries.firstOrNull { it.bytes == bytes } ?: default
    }
}
