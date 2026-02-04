package com.jocmp.capy.accounts

import kotlinx.serialization.Serializable

@Serializable
enum class Source(val value: String) {
    LOCAL("local"),
    FEEDBIN("feedbin"),
    FRESHRSS("freshrss"),
    /** Miniflux with username/password combination */
    MINIFLUX("miniflux"),
    /** Miniflux with API Token */
    MINIFLUX_TOKEN("miniflux_token"),
    READER("reader");

    val hasCustomURL
        get() = this == FRESHRSS ||
                this == MINIFLUX ||
                this == MINIFLUX_TOKEN ||
                this == READER

    val supportsLabels
        get() = this == FRESHRSS
}
