package com.jocmp.capy

enum class FeedPriority(val value: String) {
    MAIN_STREAM("main"),
    IMPORTANT("important"),
    CATEGORY("category"),
    FEED("feed");

    /**
     * Returns all priorities up to the current priority
     * for filtering
     */
    val inclusivePriorities: List<String>
        get() = entries.subList(0, ordinal + 1).map { it.value }

    companion object {
        fun parse(value: String?): FeedPriority? {
            return entries.firstOrNull { it.value == value }
        }
    }
}
