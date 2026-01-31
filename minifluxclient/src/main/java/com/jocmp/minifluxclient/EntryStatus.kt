package com.jocmp.minifluxclient

import com.squareup.moshi.Json

enum class EntryStatus(val value: String) {
    @Json(name = "read")
    READ("read"),

    @Json(name = "unread")
    UNREAD("unread"),

    @Json(name = "removed")
    REMOVED("removed")
}
