package com.jocmp.capy

enum class FeedPriority(val value: String) {
    MAIN_STREAM("main"),
    IMPORTANT("important"),
    CATEGORY("category"),
    FEED("feed");

    companion object {
        fun parse(value: String?): FeedPriority? {
            return entries.firstOrNull { it.value == value }
        }
    }
}
