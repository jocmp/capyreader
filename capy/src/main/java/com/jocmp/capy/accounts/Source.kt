package com.jocmp.capy.accounts

import kotlinx.serialization.Serializable

@Serializable
enum class Source(val value: String) {
    LOCAL("local"),
    FEEDBIN("feedbin"),
    FRESHRSS("freshrss"),
    MINIFLUX("miniflux"),
    READER("reader");

    val hasCustomURL
        get() = this == FRESHRSS ||
                this == MINIFLUX ||
                this == READER

    val supportsLabels
        get() = this == FRESHRSS
}
