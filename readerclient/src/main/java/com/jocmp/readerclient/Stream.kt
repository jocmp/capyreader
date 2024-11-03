package com.jocmp.readerclient

enum class Stream(val id: String) {
    KEPT_UNREAD("user/-/state/com.google/kept-unread"),

    READING_LIST("user/-/state/com.google/reading-list"),

    STARRED("user/-/state/com.google/starred"),

    READ("user/-/state/com.google/read")
}
