package com.jocmp.capy

/**
 * Feed importance bucket, modeled after Fraidycat.
 *
 * Higher importance = check more often, place higher in the surfaced feed list.
 */
enum class FeedImportance {
    REAL_TIME,
    DAILY,
    NORMAL,
    WEEKLY,
    MONTHLY,
    YEARLY;

    val storageValue: String
        get() = name

    companion object {
        fun parse(value: String?): FeedImportance {
            if (value.isNullOrBlank()) return NORMAL
            return entries.firstOrNull { it.name == value } ?: NORMAL
        }
    }
}
