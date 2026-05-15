package com.jocmp.capy

data class SyncStatus(
    val articleID: String,
    val key: Key,
    val flag: Boolean,
    val selected: Boolean = false,
) {
    enum class Key(val raw: String) {
        READ("read"),
        STARRED("starred"),
        CACHE("cache");

        companion object {
            fun from(raw: String): Key? = entries.find { it.raw == raw }

            val remoteSyncKeys = listOf(READ, STARRED)
        }
    }
}
