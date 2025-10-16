package com.jocmp.minifluxclient

import com.squareup.moshi.Json

enum class EntryStatus {
    @Json(name = "read")
    READ,

    @Json(name = "unread")
    UNREAD,

    @Json(name = "removed")
    REMOVED
}
